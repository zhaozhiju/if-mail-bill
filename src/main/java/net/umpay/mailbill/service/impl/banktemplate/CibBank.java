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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 兴业银行邮件账单解析
 * 
 * @author admin
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CibBank implements IBankTemplateService {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CibBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CIB;
	}
	
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl integrationDetailServiceImpl;
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;
	
	/**
	 * 兴业银行银行账单解析
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
		
		Map<String, String> map = new HashMap<String, String>();
		List<String> listString = new ArrayList<String>();
		int flag = 0;
		Long[] billId = null;
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CibBank.class, flag});
		if (flag == 1){
			Map<String, String> cibBankInfoMap = CibBankInfoMap(listString,map);
			List<List<String>> cibBankInfoList = CibBankInfoList(listString, map);
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CIB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CIB);//兴业账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2));//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(Money.toNumberic(cibBankInfoMap.get("到期还款日")), "yyyy年MM月dd日"));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("信用额度(人民币)"))));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("本期应还款总额"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.toNumberic(cibBankInfoMap.get("本期积分余额"))));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("本期最低还款额"))));
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("预借现金额度(人民币)"))));
			String[] split = Money.toNumberic(cibBankInfoMap.get("账单周期")).split("-");
			billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
			billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1]));// 周期的拆分
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));//账单日
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(split[1]),"yyyyMM"));
			billCycleInfoEntity.setCardEndOfFour(map.get("主卡卡号").toString());
			billCycleInfoEntity.setUserName(Money.toNumberic(cibBankInfoMap.get("姓名")));
			if (!StringUtils.isBlank(cibBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(Money.toNumberic(cibBankInfoMap.get("性别")));
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
			
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("上期账单余额"))));
			balanceDetailEntity.setInterest(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("循环利息"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("已还金额"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("本期账单金额"))));
			balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("本期调整金额"))));
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(cibBankInfoMap.get("本期应还款总额"))));
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.toNumberic(cibBankInfoMap.get("上期积分余额"))));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.toNumberic(cibBankInfoMap.get("本期新增积分"))));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.toNumberic(cibBankInfoMap.get("本期积分余额"))));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(Money.toNumberic(cibBankInfoMap.get("本期调整积分(含到期失效积分)"))));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.toNumberic(cibBankInfoMap.get("本期兑换积分(含消费积分)"))));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			if (update)
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			int size = cibBankInfoList.size();
			for (int i = 0; i < size; i++){
				List<String> dateList = cibBankInfoList.get(i);
				for (int j = 0; j < dateList.size();){
					BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
					if (update){
						bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
						bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
					}
					bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
					bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
					String cardEndOfFour = map.get("主卡卡号").toString();
					bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
					String string = dateList.get(j);
					if (string.contains(":")){
						String substring = string.substring(0, 10);
						String sub = string.substring(11, 15);
						string = substring + " " + sub;
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(string, "yyyy-MM-dd HH:mm"));
					}else {
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(string, "yyyy-MM-dd"));
					}
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy-MM-dd"));
					bankMonthDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(j+2)));
					String strInput = dateList.get(j+3);
					if (strInput.contains("-")){
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
					}
					bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
					bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
					j += 4;
				}
			}
			return "1";
		}
		return "00";
	}

	/**
	 * 获取账单类别
	 * @param parse	之前的字符串
	 * @param listString	截取后的字符串
	 * @param flag	各个类别标准的标识
	 * @return	返回银行类别
	 */
	private int getStringList(List<String> parse, List<String> listString, int flag) {
		String replace;
		int size = parse.size();
		for(int i = 1; i < size; i++){
			if (parse.get(i).contains("尊敬的")){	
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				listString.add(replace);
			}
			if (parse.get(i).startsWith("账单周期")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.startsWith("账单说明")){
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
	 * 存储兴业银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list 处理并过滤后的HTML内容
	 * @param map	存放需要的一次性数据
	 */
	private Map<String, String> CibBankInfoMap(List<String> list, Map<String, String> map){
		getUser(list, map);
		
//		GetUserInfoUtil.getUserInfoFor(list, map, 3, 2);
		
		//账户信息开始
		String temp ;
		temp = list.get(list.indexOf("人民币账户RMBA/C")-3);
		map.put("账单周期", temp.substring(temp.indexOf("账单周期StatementCycle")+18,temp.indexOf("信用额度")));
		map.put("信用额度(人民币)", temp.substring(temp.indexOf("CreditLimit(RMB)")+16,temp.indexOf("预借现金额度")));
		map.put("预借现金额度(人民币)", temp.substring(temp.indexOf("CashAdvanceLimit(RMB)")+21));
		
		temp = list.get(list.indexOf("人民币账户RMBA/C")-2);
		map.put("到期还款日", temp.substring(temp.indexOf("到期还款日PaymentDueDate")+19,temp.indexOf("本期应还款总额")));
		map.put("本期应还款总额", temp.substring(temp.indexOf("本期应还款总额NewBalance")+17,temp.indexOf("本期最低还款额")));
		map.put("本期最低还款额", temp.substring(temp.indexOf("本期最低还款额MinimumPayment")+21));
		
		//积分换礼开始（以下文积分账户为基准）
//		temp = list.get(list.indexOf("人民币账户RMBA/C")-1);
//		map.put("本月新增积分", temp.substring(temp.indexOf("本月新增积分")+7,temp.indexOf("当前积分余额")-2)); //以下文积分明细为基准 
//		map.put("当前积分余额", temp.substring(temp.indexOf("当前积分余额")+7,temp.indexOf("积分不够您还可以")-2));	//以下文积分明细为基准
		
		//本期账务明细开始
//		temp = list.get(list.indexOf("人民币账户RMBA/C")+3);
//		map.put("本期应缴余额", temp.substring(temp.indexOf("本期应缴余额NewBalance")+16)); //与上面的“本期应还款总额”的值一样
		temp = list.get(list.indexOf("人民币账户RMBA/C")+5);
		map.put("上期账单余额", temp.substring(temp.indexOf("上期账单余额PreviousBalance")+21));
		temp = list.get(list.indexOf("人民币账户RMBA/C")+7);
		map.put("已还金额", temp.substring(temp.indexOf("Credit")+6));
		temp = list.get(list.indexOf("人民币账户RMBA/C")+9);
		map.put("本期账单金额", temp.substring(temp.indexOf("NewActivity")+11));
		temp = list.get(list.indexOf("人民币账户RMBA/C")+11);
		map.put("本期调整金额", temp.substring(temp.indexOf("Adjustment")+10));
		temp = list.get(list.indexOf("人民币账户RMBA/C")+13);
		map.put("循环利息", temp.substring(temp.indexOf("FinanceCharge")+13));
		
		//积分账户开始
		temp = list.get(list.indexOf("积分账户BonusPointAccount")+1);
		map.put("本期积分余额", temp.substring(temp.indexOf("BonusPointBalance")+17));
		temp = list.get(list.indexOf("积分账户BonusPointAccount")+3);
		map.put("上期积分余额", temp.substring(temp.indexOf("PreviousBonusPoint")+18));
		temp = list.get(list.indexOf("积分账户BonusPointAccount")+5);
		map.put("本期新增积分", temp.substring(temp.indexOf("NewBonusPoint")+13));
		temp = list.get(list.indexOf("积分账户BonusPointAccount")+7);
		map.put("本期调整积分(含到期失效积分)", temp.substring(temp.indexOf("AdjustedBonusPoint")+18));
		temp = list.get(list.indexOf("积分账户BonusPointAccount")+9);
		map.put("本期兑换积分(含消费积分)", temp.substring(temp.indexOf("UsedBonusPoint")+14));
		return map;
	}

	private void getUser(List<String> list, Map<String, String> map) {
		String string;
		int size = list.size();
		for(int i = 0 ; i < size; i ++){
			string = list.get(i);
			if (string.contains("尊敬的")){
				int index = string.indexOf("尊敬的");
				int indexOf = string.indexOf("您好!");
				int indexOf1 = string.indexOf("：");
				String substring = string.substring(index+3, (indexOf == -1 ? indexOf1 : indexOf)-2);
				String substring2 ;
				if (-1 != indexOf || -1 != indexOf1){
					substring2 = string.substring((indexOf == -1 ? indexOf1 : indexOf)-2, string.length());
					map.put("姓名", substring);
					if(substring2.contains("女士")){
						map.put("性别", "女");
						break;
					}else if (substring2.contains("先生")){
						map.put("性别", "男");
						break;
					}else{
						map.put("性别", "");
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 存储兴业银行信用卡消费明细（循环数据）
	 * 1.交易日期 2.记账日期 3.交易摘要 4.人民币金额 
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> CibBankInfoList(List<String> list, Map<String, String> map){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("人民币金额Amount(RMB)")+2;	//明细信息开始位置
		int end = list.indexOf("账单说明：“本期应缴余额”栏目显示为“-***”(***为金额)，是指您在还款时多缴的或您预先存放在信用卡账户内的资金，该资金可抵扣您的刷卡消费金额。Statementdescription:Ifthecolumn“NewBalance”displays“-***(where***representsaccountbalance),itindicatesthatthereisacreditbalancemaintainedinyouraccount.Nopaymentisrequired.")-1;	//明细信息结束位置
		final int count_field = 4;	//明细字段数
		String value = list.get(start-1);
		int indexOf = value.indexOf(")");
		map.put("主卡卡号", value.substring(indexOf-4, indexOf));
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
