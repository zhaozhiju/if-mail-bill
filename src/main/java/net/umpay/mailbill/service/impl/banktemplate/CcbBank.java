package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.banktemplate.IBankTemplateService;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.service.impl.resolve.BalanceDetailServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillBankMonthDetailServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillJobServiceImpl;
import net.umpay.mailbill.service.impl.resolve.IntegrationDetailServiceImpl;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.date.DateUtil;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.money.Money;
import net.umpay.mailbill.util.number.NumberTools;
import net.umpay.mailbill.util.string.ReadProperty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 建设银行邮件账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CcbBank  implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CcbBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CCB;
	}
	
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl detailServiceImpl;
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;

	@Override
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
		boolean update = false;
		// 如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		Map<String, String> map = new HashMap<String, String>();
		List<String> listString = new ArrayList<String>();
		int flag = 0;
		Long[] billId = null;
		flag = getString(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CcbBank.class, flag});
		if (flag == 1){
//			log.info("建设 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.账务汇总信息
			Map<String, String> ccbBankInfoMap = CcbBankInfoMap(listString, map);
			// 2.交易明细列表
			List<List<String>> ccbBankInfoList = CcbBankInfoList(listString);
			// 3.账单周期
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CCB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CCB);
			billCycleInfoEntity.setSenderUrl(senderUrl); // 获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String billDate = ccbBankInfoMap.get("账单日");
			if (StringUtils.isNotBlank(billDate)) {
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(billDate));
				billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(billDate),"yyyyMM"));
			}
			if (StringUtils.isNotBlank(ccbBankInfoMap.get("到期还款日"))){
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(ccbBankInfoMap.get("到期还款日")));
			}
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("信用额度"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(ccbBankInfoMap.get("积分余额"))));
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("取现额度"))));
			//判断得到的本期全部应还款额是“-”，则赋值为‘0’
			if(ccbBankInfoMap.get("本期全部应还款额").equals("-")){
				billCycleInfoEntity.setNewRmbBalance(0);
			}else{
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("本期全部应还款额"))));
			}
			if(ccbBankInfoMap.get("最低还款额").equals("-")){
				billCycleInfoEntity.setMinRmbPayment(0);
			}else{
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("最低还款额"))));//rmb
			}
			String billCycle = ccbBankInfoMap.get("账单周期");
			if (StringUtils.isNotBlank(billCycle) && billCycle.indexOf("-") != -1){
				String[] split = ccbBankInfoMap.get("账单周期").split("-");
				billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
				billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1]));// 周期的拆分
			}
			
			if(CollectionUtils.isNotEmpty(ccbBankInfoList)) { // 可能存在首行无卡号,依次往下找
				int length = ccbBankInfoList.size();
				for(int k = 0; k < length; k++){
					if(StringUtils.isNotBlank(ccbBankInfoList.get(k).get(2))){
						billCycleInfoEntity.setCardEndOfFour(ccbBankInfoList.get(k).get(2)); // 卡号末四位
						break;
					}
				}
			}
			
			billCycleInfoEntity.setUserName(ccbBankInfoMap.get("姓名"));
			if (StringUtils.isNotBlank(ccbBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(ccbBankInfoMap.get("性别"));
			}else{
				billCycleInfoEntity.setUserGender(null);
			}
			String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
			if (StringUtils.isNotBlank(emailInfo)){
				Date stringT = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
				billCycleInfoEntity.setSentData(stringT);
			}
			billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
			billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
			if(update){
				billCycleInfoEntity.setId(id[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			// 4.本期应还款详情
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			//balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("本期全部应还款额"))));
			//判断得到的本期全部应还款额是“-”，则赋值为‘0’
			if(ccbBankInfoMap.get("本期全部应还款额").equals("-")){
				balanceDetailEntity.setNewBalance(0);
			}else{
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("本期全部应还款额"))));
			}
			//判断得到的本期全部应还款额是“-”，则赋值为‘0’
			if(ccbBankInfoMap.get("上期账单余额").equals("-")){
				balanceDetailEntity.setBalance(0);
			}else{
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(ccbBankInfoMap.get("上期账单余额"))));
			}
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			// 5.积分明细开始
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = detailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setAddedPoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期消费积分"))));
			integrationDetailEntity.setAwardPoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期奖励积分"))));
			integrationDetailEntity.setBalancePoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期起初积分"))));
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setExchangePoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期兑换余额"))));
			integrationDetailEntity.setRevisePoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期调整积分"))));
			integrationDetailEntity.setUsePoints(NumberTools.getNumber(Money.getNumber(ccbBankInfoMap.get("本期积分余额"))));
			
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			detailServiceImpl.save(integrationDetailEntity);
			// 6.详细交易
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			if(CollectionUtils.isNotEmpty(ccbBankInfoList)){
				int length = ccbBankInfoList.size();
				for (int i = 0; i < length; i++){
					List<String> rowInfo = ccbBankInfoList.get(i);
					
					if(CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength; ){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j)));
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1)));
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+2));
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3));
							bankMonthDetailEntity.setCurrencyType(rowInfo.get(j+4));
							String amount = rowInfo.get(j+5);
							if (amount.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(amount)));
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += rowLength;
						}	
					}
				}
			}
			
			return "1";
		}
		
		return "00";
	}
	
	/**
	 * 截取需要的字符串
	 * @param lines	含不需要数据的字符
	 * @param listString	截取有用的
	 */
	private int getString(List<String> lines, List<String> listString, int flag) {
		String replace;
		int size = lines.size();
		for(int i = 0; i<size; i++){
			if (lines.get(i).contains("尊敬的")){	
				for (int j = i; j < size; j++){
					if (lines.get(j).contains("您好")){
						replace = lines.get(j).replace("\\s", "").replace("&nbsp;", "");
						listString.add(replace);
						break;
					}
				}
				continue;
			}
			if (lines.get(i).startsWith("账单周期")){	
				for (int jj = i; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					if(replace.contains("如您同时拥有多张主卡")){
						break;
					}
					listString.add(replace);
				}
			}
			if (lines.get(i).startsWith("应还款信息PaymentInformation")){
				for (int jj = i; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					if(replace.contains("您可微信关注“建行电子银行”")){
						i = jj;
						break;
					}
					listString.add(replace); 		//将需要的数据添加到listString里
				}
			}
			if(lines.get(i).contains("【应还款明细】请按以下卡号、币种分别还款")){
				for(int jj = i;jj < size;jj++){
					replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
					if(replace.contains("如有任何问题")){
						break;
					}
					listString.add(replace); 		//将需要的数据添加到listString里
				}
				flag = 1;
				break;
			}
		}
		return flag;
	}
	
	/**
	 * 存储建设银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list解析后的数据
	 * @param map
	 */
	private Map<String, String> CcbBankInfoMap(List<String> list, Map<String, String> map){
		int size = list.size();
		for (int i = 0; i < size; i++){
			String string = list.get(i);
			if (string.contains("尊敬的") && i < 10){
				int indexOf = string.indexOf("，");
				if (string.length() > indexOf && indexOf != -1){
					String substring = string.substring(3, indexOf);
					map.put("姓名", substring);
				}
				map.put("性别", "");
				continue;
			}
			if (i > 20){
				break;
			}
		}
		int indexOf = list.indexOf("账单日");
		if (indexOf != -1){
			//账单周期开始 对两个表格的数据进行抓取	
			String temp ;
			temp = list.get(indexOf-2);
			map.put("账单周期", temp.substring(5));
			map.put("账单日", list.get(indexOf+2));
			map.put("到期还款日", list.get(list.indexOf("到期还款日")+2));
			map.put("信用额度", list.get(list.indexOf("信用额度")+2));
			map.put("取现额度", list.get(list.indexOf("取现额度")+2));
			map.put("积分余额", list.get(list.indexOf("积分余额")+2));
		} else {
			int indexOf2 = list.indexOf("账单日StatementDate");
			if (indexOf2 != -1){
				map.put("账单日", list.get(indexOf2+1));
				map.put("到期还款日", list.get(list.indexOf("到期还款日PaymentDueDate")+1));
				map.put("信用额度", list.get(list.indexOf("信用额度CreditLimit")+1));
				map.put("取现额度", list.get(list.indexOf("取现额度CashAdvanceLimit")+1));
				map.put("积分余额", list.get(list.indexOf("积分余额AvailablePoints")+1));
				String value = list.get(list.indexOf("账户信息AccountInformation")-1);
				map.put("账单周期", value.substring(value.indexOf("：")+1, value.length()));
			}
		}
		
		map.put("账户币种", list.get(list.indexOf("账户币种Currency")+4));
		map.put("本期全部应还款额", list.get(list.indexOf("本期全部应还款额NewBalance")+4));
		map.put("最低还款额", list.get(list.indexOf("最低还款额Min.Payment")+4));
		map.put("争议款/笔数", list.get(list.indexOf("争议款/笔数DisputeAmt/Nbr")+4));//
		
		//应还款明细开始
		map.put("信用卡卡号", list.get(list.indexOf("【积分明细】")+15));
//		map.put("账户币种_应还款明细_1", list.get(list.indexOf("信用卡卡号CardNumber")+8)); 	//与上面的”账户币种“值相同
//		map.put("应还款额/溢缴款_应还款明细_1", list.get(list.indexOf("信用卡卡号CardNumber")+9));	//与上面的”本期全部应还款额“值相同
//		map.put("最低还款额_应还款明细_1", list.get(list.indexOf("信用卡卡号CardNumber")+10));	//与上面的”最低还款额“值相同
//		map.put("账户币种_应还款明细_2", list.get(list.indexOf("信用卡卡号CardNumber")+11));	//与上面的”账户币种“值相同
//		map.put("应还款额/溢缴款_应还款明细_2", list.get(list.indexOf("信用卡卡号CardNumber")+12));	//该条目再次出现
//		map.put("最低还款额_应还款明细_2", list.get(list.indexOf("信用卡卡号CardNumber")+13));	//该条目再次出现
		
		//交易明细为循环数据 通过CcbBankInfoList方法存储
		map.put("上期账单余额", list.get(list.indexOf("上期账单余额(PreviousBalance)")+1));
		
		//积分明细开始
//		map.put("信用卡卡号_积分明细", list.get(list.indexOf("本期起初积分")+13));	//与上面的”信用卡卡号“值相同
		map.put("本期起初积分", list.get(list.indexOf("本期起初积分")+14));
		map.put("本期消费积分", list.get(list.indexOf("本期消费积分")+14));
		map.put("本期奖励积分", list.get(list.indexOf("本期奖励积分")+14));
		map.put("本期调整积分", list.get(list.indexOf("本期调整积分")+14));
		map.put("本期兑换余额", list.get(list.indexOf("本期兑换余额")+14));
		map.put("本期积分余额", list.get(list.indexOf("本期积分余额")+14));
		
		return map;
	}
	
	/**
	 * 存储建行消费明细（循环数据）
	 * 1.交易日 2.银行记账日 3.卡号后四位 4.交易描述 5.（空）6.（空） 7.交易币/金额 8.结算币/金额
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> CcbBankInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("上期账单余额(PreviousBalance)")+2;	//明细信息开始位置
		int end = list.indexOf("***结束TheEnd***")-1;	//明细信息结束位置
		final int count_field = 8;	//明细字段数
		int listSize = (end+1 - start )/count_field;	//记录明细信息的条数
		for(int i=0;i<listSize;i++){
			List<String> listtemp = new ArrayList<String>();
			for(int j=0;j<count_field;j++){		//每条明细的摘要后都会多余两个空的元素 跳过处理
				if(list.get(start).equals("")){
					start++;
					continue;
				}
				listtemp.add(list.get(start));
				start++;
			}
			listDouble.add(listtemp);
		}
		return listDouble;
	}
}
