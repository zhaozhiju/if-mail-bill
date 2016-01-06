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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 北京银行账单解析
 * 
 * @author admin
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BeiJingBank implements IBankTemplateService {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(BeiJingBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_BEIJING;
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
	 * 北京银行账单解析
	 * 
	 * @param parse 处理后的HTML内容
	 * @return String
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
		
		flag = getStingList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{BeiJingBank.class, flag});
		if (flag == 1){
			String emailUrl = ReadProperty.getEmailUrl(oldHTML[0],1);
			Map<String, String> keyMap = keyMap(listString,map);
			List<List<String>> list = list(listString);
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_BEIJING);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_BEIJING);//账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(senderUrl);//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(emailUrl);
			String billDate = keyMap.get("本期账单日");
			if (!StringUtils.isBlank(billDate))
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(billDate));
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(billDate),"yyyyMM"));
			String paymentDueDate = keyMap.get("到期还款日");
			if (!StringUtils.isBlank(paymentDueDate))
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(paymentDueDate));
			String cashRmbAdvanceLimit = keyMap.get("预借现金额度");
			if (!StringUtils.isBlank(cashRmbAdvanceLimit))
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.toNumberic(cashRmbAdvanceLimit)));
			String rmbCreditLimit = keyMap.get("信用额度");
			if (!StringUtils.isBlank(rmbCreditLimit))
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(rmbCreditLimit)));
			String newRmbBalance = keyMap.get("本期应还款金额");
			if (!StringUtils.isBlank(newRmbBalance))
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(newRmbBalance)));
			String minRmbPayment = keyMap.get("最低还款金额");
			if (!StringUtils.isBlank(minRmbPayment))
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(minRmbPayment)));
			String rmbIntegration = keyMap.get("本期账单积分余额");
			if (!StringUtils.isBlank(rmbIntegration))
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.toNumberic(rmbIntegration)));
			billCycleInfoEntity.setCardEndOfFour(list.get(0).get(4).toString());
			String userName = keyMap.get("姓名");
			if (!StringUtils.isBlank(userName))
				billCycleInfoEntity.setUserName(Money.toNumberic(userName));
			if (!StringUtils.isBlank(keyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(Money.toNumberic(keyMap.get("性别")));
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
			String balance = keyMap.get("上期账单金额");
			if (!StringUtils.isBlank(balance))
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(balance)));
			String payment = keyMap.get("上期还款金额");
			if (!StringUtils.isBlank(payment))
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(payment)));
			String newCharges = keyMap.get("本期账单金额");
			if (!StringUtils.isBlank(newCharges))
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(newCharges)));
			String interest = keyMap.get("循环利息");
			if (!StringUtils.isBlank(interest))
				balanceDetailEntity.setInterest(NumberTools.createDouble(Money.toNumberic(interest)));
			String adjustment = keyMap.get("本期调整金额");
			if (!StringUtils.isBlank(adjustment))
				balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(adjustment)));
			if (!StringUtils.isBlank(newRmbBalance))
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(newRmbBalance)));
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if (!StringUtils.isBlank(rmbIntegration))
				integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.toNumberic(rmbIntegration)));
			String exchangePoints = keyMap.get("本期兑换积分");
			if (!StringUtils.isBlank(exchangePoints))
				integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.toNumberic(exchangePoints)));
			String addedPoints = keyMap.get("本期新增积分");
			if (!StringUtils.isBlank(addedPoints))
				integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.toNumberic(addedPoints)));
			String revisePoints = keyMap.get("本期调整积分");
			if (!StringUtils.isBlank(revisePoints))
				integrationDetailEntity.setRevisePoints(NumberTools.toInt(Money.toNumberic(revisePoints)));
			String balancePoints = keyMap.get("上期积分余额");
			if (!StringUtils.isBlank(balancePoints))
				integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.toNumberic(balancePoints)));
			String awardPoints = keyMap.get("本期奖励积分");
			if (!StringUtils.isBlank(awardPoints))
				integrationDetailEntity.setAwardPoints(NumberTools.toInt(Money.toNumberic(awardPoints)));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			if (update)
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			int size = list.size();
			for (int i = 0; i < size; i++){
				List<String> dateList = list.get(i);
				for (int j = 0; j < dateList.size(); ){
					BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
					if (update){
						bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
						bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
					}
					bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
					bankMonthDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(j+2)));
					String strInput = dateList.get(j+3);
					if (strInput.contains("-")){
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
					}
					bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
					bankMonthDetailEntity.setCardEndOfFour(dateList.get(j+4));
					bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
					bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
					j += 5;
				}
			}
			return "1";
		}
		return "00";
	}
	/**
	 * 获取有需要的字符串List
	 * @param parse 原始字符串List
	 * @param listString 存放截取后的字符串
	 * @param flag 标示此银行的账单类别
	 * @return flag
	 */
	private int getStingList(List<String> parse, List<String> listString, int flag) {
		String replace;
		int size = parse.size();
		for(int i = 0; i < size; i++){
			if (parse.get(i).contains("账户信息提示")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.contains("网站首页")){
						flag = 1;
						break;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 存储北京银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list 处理并过滤后的HTML内容
	 * @param map	存放需要的一次性数据
	 */
	private Map<String, String> keyMap(List<String> list, Map<String, String> map){
		int k =0;
		int size = list.size();
		for (int i = 0; i < size; i++){
			String string = list.get(i);
			if (string.contains("账户信息提示")){
				String[] split = string.split(":");
				if (!StringUtils.isBlank(split[1]) && split[1].length() >= 11)
				map.put("本期账单日", split[1].substring(0, 11));
				if (!StringUtils.isBlank(split[2]) && split[2].length() >= 11)
					map.put("到期还款日", split[2].substring(0, 11));
				int indexOf2 = split[3].indexOf("元");
				if (indexOf2 != -1 && split[3].length() > 5)
					map.put("信用额度", split[3].substring(5, indexOf2));
				int indexOf = split[4].indexOf("元");
				if (indexOf != -1 && split[4].length() > 5)
					map.put("预借现金额度", split[4].substring(5, indexOf));
				continue;
			}
			GetUserInfoUtil.getUserInfo(map, string, 3, 3);
			if (string.contains("本期应还款金额")  && k == 0){
				if (i+1 < size)
					map.put("本期应还款金额", list.get(i+1).substring(0,list.get(i+1).length()-1));
				k = 1;
				continue;
			}
			if (string.contains("最低还款金额")){
				if (i+1 < size)
					map.put("最低还款金额", list.get(i+1).substring(0,list.get(i+1).length()-1));
				continue;
			}
			if (string.contains("上期账单金额")){
				if (i+11 < size)
					map.put("上期账单金额", list.get(i+11));
				continue;
			}
			if (string.contains("上期还款金额")){
				if (i+11 < size)
					map.put("上期还款金额", list.get(i+11));
				continue;
			}
			if (string.contains("本期账单金额")){
				if (i+11 < size)
					map.put("本期账单金额", list.get(i+11));
				continue;
			}
			if (string.contains("本期调整金额")){
				if (i+11 < size)
					map.put("本期调整金额", list.get(i+11));
				continue;
			}
			if (string.contains("循环利息")){
				if (i+11 < size)
					map.put("循环利息", list.get(i+11));
				continue;
			}
			if (string.contains("本期账单积分余额")){
				if (i+11 < size)
					map.put("本期账单积分余额", list.get(i+11));
				continue;
			}
			if (string.contains("上期积分余额")){
				if (i+11 < size)
					map.put("上期积分余额", list.get(i+11));
				continue;
			}
			if (string.contains("本期新增积分")){
				if (i+11 < size)
					map.put("本期新增积分", list.get(i+11));
				continue;
			}
			if (string.contains("本期奖励积分")){
				if (i+11 < size)
					map.put("本期奖励积分", list.get(i+11));
				continue;
			}
			if (string.contains("本期调整积分")){
				if (i+11 < size)
					map.put("本期调整积分", list.get(i+11));
				continue;
			}
			if (string.contains("本期兑换积分")){
				if (i+11 < size)
					map.put("本期兑换积分", list.get(i+11));
				break;
			}
		}
		
		return map;
	}
	
	/**
	 * 存储北京银行消费明细（循环数据）
	 * 1.交易日期 2.记账日期 3.交易摘要 4.交易金额5.卡号末四位 
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> list(List<String> list){
		List<List<String>> list2 = new ArrayList<List<String>>();
		int size = list.size();
		for (int i = 0; i < size; i++){
			if (list.get(i).contains("交易日")){
				i += 5;
				for (int j = i ; j < size; j++){
					if (StringUtils.isBlank(list.get(j)) && list.get(j+1).contains("账单说明")){
						break;
					}
					//交易详细存放如二维数组内
					List<String> tempList = new ArrayList<String>();//临时存储数据
					for(int a = 0; a < 5; a++){
						int cou = j + a;
						tempList.add(list.get(cou));//以cou为一组放入tempList内
					}
					j += 5 - 1;
					list2.add(tempList);//将tempList放入list内形成交易详细的二维数组
				}
			}
		}
		return list2;
	}
}
