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
import net.umpay.mailbill.util.string.GetUserInfoUtil;
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
 * 工商银行账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class IcbcBank implements IBankTemplateService {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(IcbcBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_ICBC;
	}
	
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl integrationDetailServiceImpl;
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;
	
	/**
	 * 工商银行账单解析
	 * 
	 * @param parse 处理后的HTML内容
	 * @return	String
	 */
	@Override
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
		
		boolean update = false;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		Map<String, String> collectInfoMap = new HashMap<String, String>();
		List<String> listString = new ArrayList<String>();
		int flag = 0;
		Long[] billId = null;
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{IcbcBank.class, flag});
		if (flag == 1){
//			log.info("ICBC card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.汇总信息
			Map<String, String> icbcBankInfoMap = IcbcBankInfoMap(listString, collectInfoMap);
			// 2.交易明细列表
			List<List<String>> detailList = IcbcBankMainCardInfoList(listString);
			// 3.账单周期表
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_ICBC);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_ICBC);//工商账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(senderUrl);//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String[] split = icbcBankInfoMap.get("账单周期").split("-");
			if (split.length != 2){
				split = icbcBankInfoMap.get("账单周期").split("—");
			}
			billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
			Date stringToDate = DateUtil.stringToDate(split[1]);
			billCycleInfoEntity.setBillCycleEnd(stringToDate);// 周期的拆分
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));//账单日
			String formatDate = DateUtil.getFormatDate(stringToDate,"yyyyMM");
			billCycleInfoEntity.setAccountOfDate(formatDate);
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(icbcBankInfoMap.get("合计_本期余额_人民币"))));
			billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(icbcBankInfoMap.get("合计_本期余额_美元"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(icbcBankInfoMap.get("个人综合积分"))));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(icbcBankInfoMap.get("到期还款日")));
			billCycleInfoEntity.setCardEndOfFour(detailList.get(0).get(0).toString());
			billCycleInfoEntity.setUserName(icbcBankInfoMap.get("姓名"));
			if (!StringUtils.isBlank(icbcBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(icbcBankInfoMap.get("性别"));
			}else{
				billCycleInfoEntity.setUserGender(null);
			}
			String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
			if (!StringUtils.isBlank(emailInfo)){
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
			// 4.积分详情
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(icbcBankInfoMap.get("联名积分_新增")));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(icbcBankInfoMap.get("个人综合积分")));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			// 5. 人民币/美元汇总信息列表
			List<List<String>> icbcBankRMBInfoList = IcbcBankRMBInfoList(listString);
			List<List<String>> icbcBankUSDInfoList = IcbcBankUSDInfoList(listString);
			// 人民币账单金额汇总信息
			if(CollectionUtils.isNotEmpty(icbcBankRMBInfoList)){
				int length = icbcBankRMBInfoList.size();
				for (int i = 0; i < length;){
					List<String> rowInfo = icbcBankRMBInfoList.get(i);
					if (CollectionUtils.isNotEmpty(rowInfo)){
						BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
						if (update){
							billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
							balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
						}
						balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
						balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+1))));
						balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+2))));
						balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+3))));
						balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+4))));
						balanceDetailServiceImpl.save(balanceDetailEntity);
					}
					break;
				}
			}
			
			// 美元账单金额汇总信息
			if(CollectionUtils.isNotEmpty(icbcBankUSDInfoList)){
				int length = icbcBankUSDInfoList.size();
				for (int i = 0; i < length;){
					List<String> rowInfo = icbcBankUSDInfoList.get(i);
					if (CollectionUtils.isNotEmpty(rowInfo)){
						BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
						balanceDetailEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
						balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+1))));
						balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+2))));
						balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+3))));
						balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(rowInfo.get(i+4))));
						balanceDetailServiceImpl.save(balanceDetailEntity);
					}
					break;
				}
			}
			
			if (update){
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			}
			
			// 6.交易明细列表
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int i = 0; i < length; i++){
					List<String> rowInfo = detailList.get(i);
					if (CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength; ){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j));
							String dateStr = rowInfo.get(j+1);
							if (dateStr.equals("&nbsp;")){
								bankMonthDetailEntity.setMerchandiseDate(null);
							}else{
								bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateStr));
							}
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+2)));
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3)+rowInfo.get(j+4));
//							if(rowInfo.get(j+5).contains("/RMB")){
//								bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(rowInfo.get(j+5).replace("/RMB", ""))));
//							}
							String amount = rowInfo.get(j+6);
							if(StringUtils.isNotBlank(amount)){
								if(amount.indexOf("/") != -1) {
									String type = amount.split("/")[1];
									String curr = type.substring(0, 3);
									if (StringUtils.isNotBlank(curr) && curr.equalsIgnoreCase("RMB")){
										bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE); // 币种
									} else {
										bankMonthDetailEntity.setCurrencyType(curr.toUpperCase()); // 币种
									}
								}
								if (amount.contains("支出")){
									bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(amount)));
								}
								if (amount.contains("存入")){
									bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME); // 存入
									bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(amount)));
								}
							}
							
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
	private int getStringList(List<String> parse, List<String> listString, int flag) {
		String replace;
		int size = parse.size();
		for(int i = 1; i<size; i++){
			if (parse.get(i).contains("尊敬的")){	
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				//详细内容的提取
				listString.add(replace);
			}
			if (parse.get(i).contains("贷记卡到期还款日")){	
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				//详细内容的提取
				listString.add(replace);
				replace = parse.get(i+2).replace("\\s", "").replace("&nbsp;", "");
				//详细内容的提取
				listString.add(replace);
			}
			if (parse.get(i).startsWith("账单周期")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.startsWith("温馨提示")){
						flag = 1;
						break;
					}
				}
				return flag;
			}
		}
		return -1;
	}
	
	/**
	 * 存储工商银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list解析后的数据
	 * @param map
	 */
	private Map<String,String> IcbcBankInfoMap(List<String> list, Map<String, String> map){
		
		GetUserInfoUtil.getUserInfoFor(list, map, 3, 6);
		
		//账单周期开始
		String temp ;
		temp = list.get(list.indexOf("本期交易汇总")-2);
		map.put("账单周期", temp.substring(temp.indexOf("账单周期")+4));
		temp = list.get(list.indexOf("本期交易汇总")-1);
		map.put("对账单生成日", temp.substring(temp.indexOf("对账单生成日")+6));
		
		map.put("到期还款日", list.get(list.indexOf("贷记卡到期还款日")+1));
		//交易汇总为循环数据 这里只抓取合计值
		temp = list.get(list.indexOf("---美元---")-4);
		map.put("合计_上期余额_人民币",temp.substring(0, temp.indexOf("/RMB")));
		temp = list.get(list.indexOf("---美元---")-3);
		map.put("合计_本期收入_人民币",temp.substring(0, temp.indexOf("/RMB")));
		temp = list.get(list.indexOf("---美元---")-2);
		map.put("合计_本期支出_人民币",temp.substring(0, temp.indexOf("/RMB")));
		temp = list.get(list.indexOf("---美元---")-1);
		map.put("合计_本期余额_人民币",temp.substring(0, temp.indexOf("/RMB")));
		
		temp = list.get(list.indexOf("人民币（本位币）交易明细")-4);
		map.put("合计_上期余额_美元",temp.substring(0, temp.indexOf("/USD")));
		temp = list.get(list.indexOf("人民币（本位币）交易明细")-3);
		map.put("合计_本期收入_美元",temp.substring(0, temp.indexOf("/USD")));
		temp = list.get(list.indexOf("人民币（本位币）交易明细")-2);
		map.put("合计_本期支出_美元",temp.substring(0, temp.indexOf("/USD")));
		temp = list.get(list.indexOf("人民币（本位币）交易明细")-1);
		map.put("合计_本期余额_美元",temp.substring(0, temp.indexOf("/USD")));
		
		//积分信息开始
		String string = list.get(list.indexOf("个人综合积分")+1);
		map.put("个人综合积分", string.substring(2, string.length()));
		map.put("联名积分_新增", list.get(list.indexOf("个人综合积分")+4));
		map.put("联名积分_余额", list.get(list.indexOf("个人综合积分")+5));
		
		return map;
	}
	
	/**
	 * 存工商银行人民币交易汇总（循环数据）
	 * 1.卡号后四位 2.上期余额 3.本期收入 4.本期支出 5.本期余额
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> IcbcBankRMBInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("---人民币（本位币）---")+1;	//开始位置
		int end = list.indexOf("合计")-1;	//结束位置
		final int count_field = 5;	//字段数
		int listSize = (end+1 - start )/count_field;	//记录条数
		for(int i=0;i<listSize;i++){
			List<String> listtemp = new ArrayList<String>();
			for(int j=0;j<count_field;j++){		//将数据中的“/RMB”去掉
				if(list.get(start).contains("/RMB")){
					listtemp.add(list.get(start).replace("/RMB", ""));
					start++;
				}
				else{
					listtemp.add(list.get(start));
					start++;
				}
			}
			listDouble.add(listtemp);
		}
		return listDouble;
	}
	
	/**
	 * 存工商银行美元交易汇总（循环数据）
	 * 1.卡号后四位 2.上期余额 3.本期收入 4.本期支出 5.本期余额
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> IcbcBankUSDInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("---美元---")+1;	//开始位置
		int end = list.lastIndexOf("合计")-1;	//结束位置
		final int count_field = 5;	//字段数
		int listSize = (end+1 - start )/count_field;	//记录条数
		for(int i=0;i<listSize;i++){
			List<String> listtemp = new ArrayList<String>();
			for(int j=0;j<count_field;j++){		//将数据中的“/USD”去掉
				if(list.get(start).contains("/USD")){
					listtemp.add(list.get(start).replace("/USD", ""));
					start++;
				}
				else{
					listtemp.add(list.get(start));
					start++;
				}
			}
			listDouble.add(listtemp);
		}
		return listDouble;
	}

	/**
	 * 存储工商银行主卡消费明细（循环数据）
	 * 1.卡号后四位 2.交易日 3.记账日 4.交易类型 5.商户名称/城市 6.交易金额/币种 7.记账金额/币种 
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> IcbcBankMainCardInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("---主卡明细---")+1;	//明细信息开始位置
		int end = list.indexOf("积分信息")-1;	//明细信息结束位置
		final int count_field = 7;	//明细字段数
		int listSize = (end+1 - start )/count_field;	//记录明细信息的条数
		for(int i=0;i<listSize;i++){
			List<String> listtemp = new ArrayList<String>();
			for(int j=0;j<count_field;j++){
				listtemp.add(list.get(start));
				start++;
			}
			listDouble.add(listtemp);
		}
		return listDouble;
	}
}
