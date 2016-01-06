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
 * 浦发银行账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SpdBank implements IBankTemplateService {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(SpdBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_SPD;
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
	
	/**
	 * 浦发银行账单解析
	 * 
	 * @param parse 处理后的HTML内容
	 * @return String
	 */
	@Override
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] idUpdate, Long accountId, Long scVersion) throws MailBillException {
		boolean update = false;
		//如果id存在的话则清除表中的数据，重新插入数据
		if (idUpdate.length != 0){
			update = true;
		}
		
		Map<String, String> billInfoMap = new HashMap<String, String>();
		List<String> listString = new ArrayList<String>();
		Long[] billId = null;
		// 账单类型判断
		int flag = getStringList(parse, billInfoMap, listString);
		log.info("calss:{} \tbankType:{}", new Object[]{SpdBank.class, flag});
		if (flag == 1){
//			log.info("浦发 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.账务信息
			Map<String, String> spdBankInfoMap = spdBankInfoMap(listString, billInfoMap);
			// 2.账户信息开始
			List<List<String>> detailList = spdBankInfoList(listString);
			// 3.账单周期
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_SPD);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_SPD);
			billCycleInfoEntity.setSenderUrl(senderUrl); // 获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("信用额度"))));
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.getNumber(spdBankInfoMap.get("取现额度"))));
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(spdBankInfoMap.get("账单日")));
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.string2DateMonth(spdBankInfoMap.get("账期")), "yyyyMM"));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("本期应还款总额"))));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(spdBankInfoMap.get("到期还款日")));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("本期最低还款额")))); // rmb
			billCycleInfoEntity.setPastDueAmount(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("逾期还款额")))); // rmb
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(spdBankInfoMap.get("本期积分余额")))); // rmb
			
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int k = 0; k < length; k++){
					if (StringUtils.isNotBlank(detailList.get(k).get(3))) {
						billCycleInfoEntity.setCardEndOfFour(detailList.get(k).get(3)); // 卡号末四位
						break;
					} else {
						continue;
					}
				}
			}
			
			billCycleInfoEntity.setUserName(spdBankInfoMap.get("姓名"));
			if (!StringUtils.isBlank(spdBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(spdBankInfoMap.get("性别"));
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
			if (update){
				billCycleInfoEntity.setId(idUpdate[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			// 4.本期应还详情
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("上期应还款余额"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("已还款金额/其他入账"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("新签金额/其他费用")))); // 本期账单金额
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("本期应还款总额"))));
			balanceDetailServiceImpl.save(balanceDetailEntity);
			// 5.积分详情
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = detailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("上期积分余额"))));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期新增积分"))));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期调整积分"))));
			integrationDetailEntity.setAwardPoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期奖励积分"))));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期兑换积分"))));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期积分余额"))));
			detailServiceImpl.save(integrationDetailEntity);
			
			if (update){
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			}
			
			// 6. 交易明细
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int i = 0; i < length; i++){
					List<String> rowInfo = detailList.get(i);
					if(CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength; ){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyy/MM/dd"));
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyy/MM/dd"));
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+2));
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+3));
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
							String strInput = rowInfo.get(j+4);
							if (strInput.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += rowLength;
						}
					}
				}
			}
			
			return "1";
		}
		
		if(flag == 2){
//			log.info("白金浦发卡 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.账务信息
			Map<String, String> spdBankInfoMap = goldSpdBankInfoMap(listString, billInfoMap);
			// 2.账户信息开始
			List<List<String>> detailList = goldSpdBankInfoList(listString);
			// 3.账单周期
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_SPD);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_SPD);
			billCycleInfoEntity.setSenderUrl(senderUrl); // 获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("信用额度"))));
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.getNumber(spdBankInfoMap.get("取现额度"))));
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(spdBankInfoMap.get("账单日")));
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.string2DateMonth(spdBankInfoMap.get("账期")), "yyyyMM"));
			
			String newBalanceRU = spdBankInfoMap.get("本期应还款总额");
			if(StringUtils.isNotBlank(newBalanceRU) && newBalanceRU.indexOf("USD") != -1){
				String rmb = newBalanceRU.substring(0, newBalanceRU.indexOf("USD"));
				String usd = newBalanceRU.substring(newBalanceRU.indexOf("USD"));
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(rmb))); // 本期应还款总额rmb
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(usd))); // 本期应还款总额usd
			}
			
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(spdBankInfoMap.get("到期还款日")));
			
			String minPaymentRU = spdBankInfoMap.get("本期最低还款额");
			if(StringUtils.isNotBlank(minPaymentRU) && minPaymentRU.indexOf("USD") != -1){
				String rmb = minPaymentRU.substring(0, minPaymentRU.indexOf("USD"));
				String usd = minPaymentRU.substring(minPaymentRU.indexOf("USD"));
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(rmb))); // rmb
				billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.getNumber(usd))); // usd
			}
			
			billCycleInfoEntity.setPastDueAmount(NumberTools.createDouble(Money.getNumber(spdBankInfoMap.get("逾期还款额")))); // rmb
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(spdBankInfoMap.get("本期积分余额")))); // rmb
			
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int k = 0; k < length; k++){
					if (StringUtils.isNotBlank(detailList.get(k).get(3))) {
						billCycleInfoEntity.setCardEndOfFour(detailList.get(k).get(3)); // 卡号末四位
						break;
					} else {
						continue;
					}
				}
			}
			
			billCycleInfoEntity.setUserName(spdBankInfoMap.get("姓名"));
			if (!StringUtils.isBlank(spdBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(spdBankInfoMap.get("性别"));
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
			if (update){
				billCycleInfoEntity.setId(idUpdate[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			
			
			String balanceRU = spdBankInfoMap.get("上期应还款余额");
			String paymentRU = spdBankInfoMap.get("已还款金额/其他入账");
			String newChargesRU = spdBankInfoMap.get("新签金额/其他费用");
			String newBalanceRU2 = spdBankInfoMap.get("本期应还款总额");
			if(StringUtils.isNotBlank(balanceRU) && balanceRU.indexOf("USD") != -1){
				// 4.本期应还详情
				BalanceDetailEntity balanceDetailEntityRmb = new BalanceDetailEntity();
				if (update){
					billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
					balanceDetailEntityRmb = balanceDetailServiceImpl.getId(billId[0]);
				}
				// 人民币
				String balanceR = balanceRU.substring(0, balanceRU.indexOf("USD"));
				String paymentR = paymentRU.substring(0, paymentRU.indexOf("USD"));
				String newChargesR = newChargesRU.substring(0, newChargesRU.indexOf("USD"));
				String newBalanceR2 = newBalanceRU2.substring(0, newBalanceRU2.indexOf("USD"));
				
				balanceDetailEntityRmb.setBillCyclePkId(billCycleInfoEntity.getId());
				balanceDetailEntityRmb.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
				balanceDetailEntityRmb.setBalance(NumberTools.createDouble(Money.getNumber(balanceR))); // 上期应还款余额
				balanceDetailEntityRmb.setPayment(NumberTools.createDouble(Money.getNumber(paymentR))); // 已还款金额/其他入账
				balanceDetailEntityRmb.setNewCharges(NumberTools.createDouble(Money.getNumber(newChargesR))); // 新签金额/其他费用
				balanceDetailEntityRmb.setNewBalance(NumberTools.createDouble(Money.getNumber(newBalanceR2))); // 本期应还款总额
				balanceDetailServiceImpl.save(balanceDetailEntityRmb);
				// 美元
				// 4.本期应还详情
				BalanceDetailEntity balanceDetailEntityUsd = new BalanceDetailEntity();
				if (update){
					billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
					balanceDetailEntityUsd = balanceDetailServiceImpl.getId(billId[0]);
				}
				String balanceU = balanceRU.substring(balanceRU.indexOf("USD"));
				String paymentU = paymentRU.substring(paymentRU.indexOf("USD"));
				String newChargesU = newChargesRU.substring(newChargesRU.indexOf("USD"));
				String newBalanceU2 = newBalanceRU2.substring(newBalanceRU2.indexOf("USD"));
				
				balanceDetailEntityUsd.setBillCyclePkId(billCycleInfoEntity.getId());
				balanceDetailEntityUsd.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
				balanceDetailEntityUsd.setBalance(NumberTools.createDouble(Money.getNumber(balanceU))); // 上期应还款余额
				balanceDetailEntityUsd.setPayment(NumberTools.createDouble(Money.getNumber(paymentU))); // 已还款金额/其他入账
				balanceDetailEntityUsd.setNewCharges(NumberTools.createDouble(Money.getNumber(newChargesU))); // 新签金额/其他费用
				balanceDetailEntityUsd.setNewBalance(NumberTools.createDouble(Money.getNumber(newBalanceU2))); // 本期应还款总额
				balanceDetailServiceImpl.save(balanceDetailEntityUsd);
			}
			
			// 5.积分详情
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = detailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("上期积分余额"))));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期新增积分"))));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期调整积分"))));
			integrationDetailEntity.setAwardPoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期奖励积分"))));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期兑换积分"))));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.getNumber(spdBankInfoMap.get("本期积分余额"))));
			detailServiceImpl.save(integrationDetailEntity);
			
			if (update){
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			}
			
			// 6. 交易明细
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int i = 0; i < length; i++){
					List<String> rowInfo = detailList.get(i);
					if(CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength; ){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyy/MM/dd"));
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyy/MM/dd"));
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+2));
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+3));
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
							String strInput = rowInfo.get(j+4);
							if (strInput.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
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
	
	private int getStringList(List<String> parse, Map<String, String> map, List<String> listString)
			throws MailBillException{
		int modleType = 0;
		boolean ifXh = true; // 外层循环是否继续, true 是; false 不;
		String replace;
		if (CollectionUtils.isNotEmpty(parse)) {
			int length = parse.size();
			for(int i = 1; i < length; i++){
				if (parse.get(i).contains("尊敬的")){	
					String string = parse.get(i-1);
					map.put("账期", string.substring(1, string.length()-1)+"份");
					replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
					int indexOf = replace.indexOf("：");
					String substring = replace.substring(3, indexOf-2);
					map.put("姓名", substring);
					if (replace.contains("女士")){
						map.put("性别", "女");
					}else if (replace.contains("先生")){
						map.put("性别", "男");
					}else{
						map.put("性别", "");
					}
					continue;
				}
				if (parse.get(i).startsWith("账单日")) {
					for (int jj = i; jj < length; jj++){
						replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
						//详细内容的提取
						listString.add(replace);
						if(replace.contains("用卡活动")){ // 普通卡
							modleType = 1;
							ifXh = false;
							break;
						}
						if(replace.contains("重要信息")){ // 白金卡
							modleType = 2;
							ifXh = false;
							break;
						}
					}
				}
				if(ifXh == false){
					break;
				}
			}
		}
		
		return modleType;
	}

	/**
	 * 存储浦发银行 - 普通卡
	 * 
	 * @param list 处理并过滤后的HTML内容
	 * @param map	存放需要的一次性数据
	 */
	private Map<String, String> spdBankInfoMap(List<String> list, Map<String, String> map){
		
		//账户信息开始
		map.put("账单日", list.get(list.indexOf("账单日StatementDate")+1));
		map.put("到期还款日", list.get(list.indexOf("到期还款日PaymentDueDate")+1));
		map.put("信用额度", list.get(list.indexOf("信用额度CreditLimit")+1));
		map.put("取现额度", list.get(list.indexOf("取现额度CashAdvanceLimit")+1));
		map.put("本期应还款总额", list.get(list.indexOf("本期应还款总额TotalStatementBalance")+1));
		map.put("本期最低还款额", list.get(list.indexOf("本期最低还款额MinDueAmount")+1));
		map.put("逾期还款额", list.get(list.indexOf("逾期还款额PastDueAmount")+1));
		
		//本期应还款总额计算明细开始
		map.put("上期应还款余额", list.get(list.indexOf("上期应还款余额LastStatementBalance")+1));
		map.put("已还款金额/其他入账", list.get(list.indexOf("-已还款金额/其他入账Payment/Credit")+1));
		map.put("新签金额/其他费用", list.get(list.indexOf("+新签金额/其他费用NewCharges")+1));
//		map.put("本期应还款总额_本期应还款总额计算明细", list.get(list.indexOf("=本期应还款总额TotalStatementBalance")+1));	//与上面的“本期应还款总额”的值相同
		
		//积分信息开始
		map.put("上期积分余额", list.get(list.indexOf("上期积分余额PointsBeginningBalance")+1));
		map.put("本期新增积分", list.get(list.indexOf("+本期新增积分PointsEarnedThisMonth")+1));
		map.put("本期奖励积分", list.get(list.indexOf("+本期奖励积分BonusPointsEarnedThisMonth")+1));
		map.put("本期调整积分", list.get(list.indexOf("+本期调整积分PointsAdjustedThisMonth")+1));
		map.put("本期兑换积分", list.get(list.indexOf("-本期兑换积分PointsRedeemedThisMonth")+1));
		map.put("本期积分余额", list.get(list.indexOf("=本期积分余额TotalPointsAvailable")+1));
		return map;
	}
	
	/**
	 * 存储浦发银行 - 白金卡卡
	 * 
	 * @param list 处理并过滤后的HTML内容
	 * @param map	存放需要的一次性数据
	 */
	private Map<String, String> goldSpdBankInfoMap(List<String> list, Map<String, String> map){
		// 账户信息
		map.put("账单日", list.get(list.indexOf("账单日StatementDate")+1));
		map.put("到期还款日", list.get(list.indexOf("到期还款日PaymentDueDate")+1));
		map.put("信用额度", list.get(list.indexOf("信用额度CreditLimit")+1));
		map.put("取现额度", list.get(list.indexOf("取现额度CashAdvanceLimit")+1));
		map.put("本期应还款总额", list.get(list.indexOf("本期应还款总额TotalStatementBalance")+1)); // 如 RMB:25,000.00USD:0.00
		map.put("本期最低还款额", list.get(list.indexOf("本期最低还款额TotalMinAmountDue")+1)); // RMB:25,000.00USD:0.00
		// 本期应还款总额
		map.put("上期应还款余额", list.get(list.indexOf("上期应还款余额LastStatementBalance")+7)); // 如 RMB:25,000.00USD:0.00
		map.put("已还款金额/其他入账", list.get(list.indexOf("已还款金额/其他入账Payment/Credit")+7)); // 如 RMB:25,000.00USD:0.00
		map.put("新签金额/其他费用", list.get(list.indexOf("新签金额/其他费用NewCharges")+7)); // 如 RMB:25,000.00USD:0.00
		// 积分信息
		map.put("上期积分余额", list.get(list.indexOf("上期积分余额PointsBeginningBalance")+12));
		map.put("本期新增积分", list.get(list.indexOf("本期新增积分PointsEarnedThisMonth")+12));
		map.put("本期奖励积分", list.get(list.indexOf("本期奖励积分BounsPointsThisMonth")+12));
		map.put("本期调整积分", list.get(list.indexOf("本期调整积分PointsAdjustedThisMonth")+12));
		map.put("本期兑换积分", list.get(list.indexOf("本期兑换积分PointsRedeemedThisMonth")+12));
		map.put("本期积分余额", list.get(list.indexOf("本期积分余额TotalPointsAvailable")+1));
		return map;
	}
	
	/**
	 * 存储浦发银行消费明细  -- 普通卡 
	 * 1.交易日期 2.记账日期 3.交易摘要 4.卡号末四位 5.交易金额
	 * @param list 处理并过滤后的数据
	 */
	private List<List<String>> spdBankInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("交易金额Amount")+1;	//明细信息开始位置
		int end = list.indexOf("　　　用卡活动")-1;	//明细信息结束位置
		final int count_field = 5;	//明细字段数
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
	
	/**
	 * 存储浦发银行消费明细  -- 白金卡 
	 * 1.交易日期 2.记账日期 3.交易摘要 4.卡号末四位 5.交易金额
	 * @param list 处理并过滤后的数据
	 */
	private List<List<String>> goldSpdBankInfoList(List<String> list) throws MailBillException{
		List<List<String>> detailList = new ArrayList<List<String>>();
		int start = list.indexOf("交易金额Amount") + 1; // 明细信息开始位置
		int end = list.indexOf("上期积分余额PointsBeginningBalance") - 4; // 明细信息结束位置
		final int count_field = 5; // 列数
		int rowSize = (end + 1 - start) / count_field; // 明细条数
		for (int i = 0; i < rowSize; i++) {
			List<String> rowInfo = new ArrayList<String>();
			for (int j = 0; j < count_field; j++) {
				rowInfo.add(list.get(start));
				start++;
			}
			detailList.add(rowInfo);
		}
		
		return detailList;
	}
}
