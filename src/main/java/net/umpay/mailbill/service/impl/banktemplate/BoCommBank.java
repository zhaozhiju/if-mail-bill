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
 * 交通银行的账单解析 
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BoCommBank implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(BoCommBank.class);
	
	@Override
	public  int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_BOCOMM;
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
	
	@Override
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
//		log.info("==html detail:{}" , parse);
		boolean update = false;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		List<String> listString = new ArrayList<String>();	//存储主要内容数据
		Map<String, String> map = new HashMap<String, String>();//存储抓取出来的账单信息
		listString.clear();//清除之前的数据
		Long[] billId = null;
		
		// 交通银行模板分类
		int flag = getBankBillType(parse, listString);
		log.info("calss:{} \tbankType:{}", new Object[]{BoCommBank.class, flag});
		// 银联标准的信用卡账户信息
		if (flag == 1){
			map.clear();
//			log.info("银联 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 抓取账务说明内字段
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 抓取账户信息内的字段
			Map<String, String> mapAccountInfo = fetchKeyMapAccount(listString, map);
			// 本账户积分明细
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 主卡消费交易明细
			List<List<String>> masterDetails = this.masterCardDetails(listString, 7, map);
			// 副卡消费交易明细
			List<List<String>> supplementDetails = this.supplementCardDetails(listString, 7, map);
			// 还款交易明细
			List<List<String>> paymentDetails = this.incomeDetailsList(listString, 7);
			
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_BOCOMM);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_BOCOMM_UNIONPAY);//银联标准的交行卡账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2));//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(mapAccountInfo.get("到期还款日"), "yyyy/MM/dd"));
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("信用额度")))) {
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("信用额度"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("取现额度")))) {
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("取现额度"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("信用额度$")))) {
				billCycleInfoEntity.setUsaCreditLimit(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("信用额度$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单应还款额")))) {
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单应还款额"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单最低还款额")))) {
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单最低还款额"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额")))) {
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(fetchCredits.get("积分余额"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额$")))) {
				billCycleInfoEntity.setUsaIntegration(NumberTools.toLong(Money.getNumber(fetchCredits.get("积分余额$"))));
			}
			
			if (StringUtils.isNotBlank(mapAccountInfo.get("账单周期")) && mapAccountInfo.get("账单周期").indexOf("-") != -1) {
				String[] split = mapAccountInfo.get("账单周期").split("-");
				billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0])); //周期的拆分
				billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1])); // 周期的拆分
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1])); // 账单日
				billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(split[1]),"yyyyMM"));
			}
			if (StringUtils.isNotBlank(map.get("主卡卡号末4位"))) {
				billCycleInfoEntity.setCardEndOfFour(map.get("主卡卡号末4位"));
			}
			billCycleInfoEntity.setUserName(fetchKeyMap.get("姓名"));
			if (!StringUtils.isBlank(fetchKeyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(fetchKeyMap.get("性别"));
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
			
			// 本期应还款详情
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if(StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("上期账单应还款额")))){
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("上期账单应还款额"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("还款/退货/费用返还")))){
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("还款/退货/费用返还"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("消费/取现/其他费用")))){
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("消费/取现/其他费用"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("本期账单应还款额")))){
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("本期账单应还款额"))));
			}
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			// 3. 本期应还还款总额信息 - USD
			BalanceDetailEntity balanceDetailUsaEntity = new BalanceDetailEntity();
			if (update){
				balanceDetailUsaEntity = balanceDetailServiceImpl.getId(billId[1]);
			}
			balanceDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE); // 美元
			balanceDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("上期账单应还款额$")))) {
				balanceDetailUsaEntity.setBalance(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("上期账单应还款额$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("还款/退货/费用返还$")))) {
				balanceDetailUsaEntity.setPayment(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("还款/退货/费用返还$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("消费/取现/其他费用$")))) {
				balanceDetailUsaEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("消费/取现/其他费用$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(mapAccountInfo.get("本期账单应还款额$")))) {
				balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(mapAccountInfo.get("本期账单应还款额$"))));
			}
			balanceDetailServiceImpl.save(balanceDetailUsaEntity);
			// 积分明细
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额")))) {
				integrationDetailEntity.setUsePoints(Integer.parseInt(Money.getNumber(fetchCredits.get("积分余额")))); // 可用人民币积分
			}
			integrationDetailEntity.setImpendingFailurePoints(fetchCredits.get("到期积分"));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			// 还款交易明细
			if (CollectionUtils.isNotEmpty(paymentDetails)) {
				int size = paymentDetails.size();
				for (int i = 0; i < size; i++){
					List<String> dateList = paymentDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						
						if(StringUtils.isNotBlank(map.get("主卡卡号末4位"))) {
							String cardEndOfFour = map.get("主卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			// 主卡交易的明细
			if (CollectionUtils.isNotEmpty(masterDetails)) {
				int size2 = masterDetails.size();
				for (int i = 0; i < size2; i++){
					List<String> dateList = masterDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						if(StringUtils.isNotBlank(map.get("主卡卡号末4位"))){
							String cardEndOfFour = map.get("主卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			// 附属卡的账单交易明细
			if (CollectionUtils.isNotEmpty(supplementDetails)) {
				int size3 = supplementDetails.size();
				for (int i = 0; i < size3; i++){
					List<String> dateList = supplementDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_VICE_CARD);
						
						if (StringUtils.isNotBlank(map.get("附属卡卡号末4位"))) {
							String cardEndOfFour = map.get("附属卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			return "1";
		}
		
		// 维萨标准的信用卡账户信息
		if (flag == 2){
			map.clear();
//			log.info("visa card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 抓取账务说明汇总信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 抓取账户信息内的字段
			Map<String, String> fetchKeyMapAccount = fetchKeyMapAccount(listString, map);
			// 积分明细
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 维萨卡标准的主卡交易的明细
			List<List<String>> visaMasterList = masterCardDetails(listString, 7, map);//主卡交易明细
			// 附属卡的账单交易明细
			List<List<String>> listSupplementCard = supplementCardDetails(listString, 7, map); 
			// 还款交易明细
			List<List<String>> paymentDetails = incomeDetailsList(listString, 7);
			
			if (CollectionUtils.isNotEmpty(visaMasterList)) { // 主卡信息
				this.visaCardHandle(fetchKeyMap, fetchKeyMapAccount,
						fetchCredits, visaMasterList, paymentDetails, true,
						update, senderUrl, oldHTML, newHtml, id, accountId,
						scVersion, map);
			}
			if (CollectionUtils.isNotEmpty(listSupplementCard)) { // 副卡信息
				this.visaCardHandle(fetchKeyMap, fetchKeyMapAccount,
						fetchCredits, listSupplementCard, paymentDetails,
						false, update, senderUrl, oldHTML, newHtml, id,
						accountId, scVersion, map);
			}
			
			return "1";
		}
		
		// 交行万事达卡
		if(flag == 3){
			map.clear();
//			log.info("万事达  card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 抓取账务说明内字段
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 抓取账户信息内的字段
			Map<String, String> fetchKeyMapAccount = fetchKeyMapAccount(listString,map);
			// 积分详情
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 主卡交易的明细
			List<List<String>> masterDetails = this.masterCardDetails(listString, 7, map);
			// 附属卡的账单交易明细说明
			List<List<String>> supplementDetails = supplementCardDetails(listString, 7, map); 
			// 还款交易明细
			List<List<String>> paymentDetails = incomeDetailsList(listString, 7);
			
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_BOCOMM);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_BOCOMM_MASTERCARD); // 交通银行万事达卡账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2)); // 获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMapAccount.get("到期还款日"), "yyyy/MM/dd"));
			
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("信用额度")))) {
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("信用额度"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("信用额度$")))) {
				billCycleInfoEntity.setUsaCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("信用额度$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("取现额度")))) {
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("取现额度"))));
			}
			
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("取现额度$")))) {
				billCycleInfoEntity.setCashUsaAdvanceLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("取现额度$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单应还款额")))) {
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单应还款额"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单应还款额$")))) {
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单应还款额$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单最低还款额")))) {
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单最低还款额"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单最低还款额$")))) {
				billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单最低还款额$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("积分余额$")))) {
				billCycleInfoEntity.setUsaIntegration(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("积分余额$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("积分余额")))) {
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("积分余额"))));
			}
			
			String[] split = Money.toNumberic(fetchKeyMapAccount.get("账单周期")).split("-");
			billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
			billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1]));// 周期的拆分
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));//账单日
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(split[1]),"yyyyMM"));
			if (StringUtils.isNotBlank(map.get("主卡卡号末4位"))) {
				billCycleInfoEntity.setCardEndOfFour(map.get("主卡卡号末4位"));
			}
			billCycleInfoEntity.setUserName(fetchKeyMap.get("姓名"));
			if (!StringUtils.isBlank(fetchKeyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(fetchKeyMap.get("性别"));
			}else{
				billCycleInfoEntity.setUserGender(null);
			}
			String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
			//log.info("++++++++++++++++++++begin++++++++++++++++emailInfo:{} \tlength:{}",new Object[]{emailInfo, emailInfo.length()});
			if (!StringUtils.isBlank(emailInfo)){
				Date stringT = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
				billCycleInfoEntity.setSentData(stringT);
			}
			//log.info("++++++++++++++++++++end++++++++++++++++emailInfo:{} \tlength:{}",new Object[]{emailInfo, emailInfo.length()});
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
			if(StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额")))){
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还")))){
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用")))){
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用"))));
			}
			if(StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额")))){
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额"))));
			}
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			BalanceDetailEntity balanceDetailUsaEntity = new BalanceDetailEntity();
			if (update){
				balanceDetailUsaEntity = balanceDetailServiceImpl.getId(billId[1]);
			}
			balanceDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE); // 美元
			balanceDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额$")))) {
				balanceDetailUsaEntity.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还$")))) {
				balanceDetailUsaEntity.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还$"))));
			}
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用$")))) {
				balanceDetailUsaEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用$"))));
			}
			
			if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额$")))) {
				balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额$"))));
			}
			balanceDetailServiceImpl.save(balanceDetailUsaEntity);
			
			billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
			
			// 积分详情汇总
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[1]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额")))) {
				integrationDetailEntity.setUsePoints(Integer.parseInt(Money.getNumber(fetchCredits.get("积分余额")))); // 可用人民币积分
			}
			integrationDetailEntity.setImpendingFailurePoints(fetchCredits.get("到期积分"));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			
			// 还款交易明细
			if (CollectionUtils.isNotEmpty(paymentDetails)) {
				int size = paymentDetails.size();
				for (int i = 0; i < size; i++){
					List<String> dateList = paymentDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						if (StringUtils.isNotBlank(map.get("主卡卡号末4位"))) {
							String cardEndOfFour = map.get("主卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			// 主卡交易的明细
			if (CollectionUtils.isNotEmpty(masterDetails)) {
				int size2 = masterDetails.size();
				for (int i = 0; i < size2; i++){
					List<String> dateList = masterDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						if (StringUtils.isNotBlank(map.get("主卡卡号末4位"))) {
							String cardEndOfFour = map.get("主卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			// 附属卡的账单交易明细说明
			if (CollectionUtils.isNotEmpty(supplementDetails)) {
				int size3 = supplementDetails.size();
				for (int i = 0; i < size3; i++){
					List<String> dateList = supplementDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_VICE_CARD);
						if (StringUtils.isNotBlank(map.get("附属卡卡号末4位"))) {
							String cardEndOfFour = map.get("附属卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			return "1";
		}
		
		// 无分类信用卡账户信息
		if (flag == 4){
			map.clear();
//			log.info("no type card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"--"+listString.get(k));
//			}
			// 抓取账务说明汇总信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 抓取账户信息内的字段
			Map<String, String> fetchKeyMapAccount = fetchKeyMapAccount(listString, map);
			// 积分明细
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 维萨卡标准的主卡交易的明细
			List<List<String>> visaMasterList = masterCardDetails(listString, 7, map); // 主卡交易明细
			// 附属卡的账单交易明细
			List<List<String>> listSupplementCard = supplementCardDetails(listString, 7, map); 
			// 无还款交易明细 List<List<String>> paymentDetails = null;
			
			if (CollectionUtils.isNotEmpty(visaMasterList)) { // 主卡信息
				this.visaCardHandle(fetchKeyMap, fetchKeyMapAccount,
						fetchCredits, visaMasterList, null, true,
						update, senderUrl, oldHTML, newHtml, id, accountId,
						scVersion, map);
			}
			if (CollectionUtils.isNotEmpty(listSupplementCard)) { // 副卡信息
				this.visaCardHandle(fetchKeyMap, fetchKeyMapAccount,
						fetchCredits, listSupplementCard, null,
						false, update, senderUrl, oldHTML, newHtml, id,
						accountId, scVersion, map);
			}
			
			return "1";
		}
		
		return "00";
	}

	/*
	 * 处理主副卡存储
	 * 
	 * @param fetchKeyMap		抓取账务说明汇总信息
	 * @param fetchKeyMapAccount抓取账户信息内的字段
	 * @param fetchCredits		积分明细
	 * @param visaDetails		主卡交易的明细
	 * @param paymentDetails	还款交易明细
	 * @param isMaster			主卡or副卡, true:主卡, false:副卡
	 * @param senderUrl			发件人地址
	 * @param oldHTML			原始的拼接地址信息
	 * @param newHtml			截取后拼接的地址信息
	 * @param id				更新时的id
	 * @param accountId			用户账号
	 * @param scVersion			服务端数据版本号
	 */
	public void visaCardHandle(Map<String, String> fetchKeyMap,
			Map<String, String> fetchKeyMapAccount,
			Map<String, String> fetchCredits,
			List<List<String>> visaDetails,
			List<List<String>> paymentDetails,
			boolean isMaster, boolean update, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion, Map<String,String> map) 
					throws MailBillException{
		Long[] billId = null;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		// 1. 账单周期表
		BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
		billCycleInfoEntity.setAccountId(accountId);
		billCycleInfoEntity.setScVersion(scVersion);
		billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_BOCOMM);
		billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
		billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_BOCOMMVISA);// 交行VISA卡
		billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
		billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2)); // 获取发件人地址
		billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
		billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
		billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
		billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
		billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
		billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(Money.toNumberic(fetchKeyMapAccount.get("到期还款日")), "yyyy/MM/dd"));
		if(StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("信用额度")))) {
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("信用额度"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("信用额度$")))) {
			billCycleInfoEntity.setUsaCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("信用额度$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("取现额度")))) {
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("取现额度"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("取现额度$")))) {
			billCycleInfoEntity.setCashUsaAdvanceLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("取现额度$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单应还款额")))) {
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单应还款额"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单应还款额$")))) {
			billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单应还款额$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单最低还款额")))) {
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单最低还款额"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("本期账单最低还款额$")))) {
			billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期账单最低还款额$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("积分余额$")))) {
			billCycleInfoEntity.setUsaIntegration(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("积分余额$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMap.get("积分余额")))) {
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("积分余额"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额$")))) {
			billCycleInfoEntity.setUsaIntegration(NumberTools.toLong(Money.getNumber(fetchCredits.get("积分余额$"))));
		}
		
		if(StringUtils.isNotBlank(fetchKeyMapAccount.get("账单周期")) 
				&& fetchKeyMapAccount.get("账单周期").indexOf("-") != -1) {
			String[] split = fetchKeyMapAccount.get("账单周期").split("-");
			billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
			billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1]));// 周期的拆分
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));//账单日
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(split[1]),"yyyyMM"));
		}
		if(isMaster == true){ // 主卡
			if(StringUtils.isNotBlank(map.get("主卡卡号末4位"))){
				billCycleInfoEntity.setCardEndOfFour(map.get("主卡卡号末4位"));
			}
		} else { // 副卡
			if(StringUtils.isNotBlank(map.get("附属卡卡号末4位"))){
				billCycleInfoEntity.setCardEndOfFour(map.get("附属卡卡号末4位"));
			}
		}

		billCycleInfoEntity.setUserName(fetchKeyMap.get("姓名"));
		if (!StringUtils.isBlank(fetchKeyMap.get("性别"))){
			billCycleInfoEntity.setUserGender(fetchKeyMap.get("性别"));
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
		
		// 2. 本期应还还款总额信息 - RMB
		BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
		if (update){
			billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
			balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
		}
		balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额")))) {
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额"))));	
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还")))) {
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用")))) {
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额")))) {
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额"))));
		}
		balanceDetailServiceImpl.save(balanceDetailEntity);
		
		// 3. 本期应还还款总额信息 - USD
		BalanceDetailEntity balanceDetailUsaEntity = new BalanceDetailEntity();
		if (update){
			balanceDetailUsaEntity = balanceDetailServiceImpl.getId(billId[1]);
		}
		balanceDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE); // 美元
		balanceDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额$")))) {
			balanceDetailUsaEntity.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("上期账单应还款额$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还$")))) {
			balanceDetailUsaEntity.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("还款/退货/费用返还$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用$")))) {
			balanceDetailUsaEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("消费/取现/其他费用$"))));
		}
		if (StringUtils.isNotBlank(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额$")))) {
			balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapAccount.get("本期账单应还款额$"))));
		}
		balanceDetailServiceImpl.save(balanceDetailUsaEntity);
		
		billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
		
		// 4. 积分汇总信息
		IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
		if (update){
			integrationDetailEntity = integrationDetailServiceImpl.getId(billId[1]);
		}
		integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		if (StringUtils.isNotBlank(Money.getNumber(fetchCredits.get("积分余额")))) {
			integrationDetailEntity.setUsePoints(Integer.parseInt(Money.getNumber(fetchCredits.get("积分余额")))); // 可用人民币积分
		}
		integrationDetailEntity.setImpendingFailurePoints(fetchCredits.get("到期积分"));
		integrationDetailServiceImpl.save(integrationDetailEntity);
		
		billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
		
		if (isMaster == true){
			if (CollectionUtils.isNotEmpty(paymentDetails)) {
				// 5. 月账单详情信息 - 还款明细
				int size = paymentDetails.size();
				for (int i = 0; i < size; i++){
					List<String> dateList = paymentDetails.get(i);
					for (int j = 0; j < dateList.size();){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						if(StringUtils.isNotBlank(map.get("主卡卡号末4位"))){
							String cardEndOfFour = map.get("主卡卡号末4位");
							bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
						}
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
						bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
						bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
						bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 7;
					}
				}
			}
			
			// 6. 月账单详情信息 - 主卡消费明细
			int size2 = visaDetails.size();
			for (int i = 0; i < size2; i++){
				List<String> dateList = visaDetails.get(i);
				for (int j = 0; j < dateList.size();){
					BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
					if (update){
						bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
						bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
					}
					bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
					if(StringUtils.isNotBlank(map.get("主卡卡号末4位"))){
						String cardEndOfFour = map.get("主卡卡号末4位").toString();
						bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
					}
					bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
					bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
					bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
					bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
					bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
					bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
					j += 7;
				}
			}
		} else {
			// 7. 月账单详情信息 - 附属卡的账单消费明细
			int size3 = visaDetails.size();
			for (int i = 0; i < size3; i++){
				List<String> dateList = visaDetails.get(i);
				for (int j = 0; j < dateList.size();){
					BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
					bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_VICE_CARD);
					if(StringUtils.isNotBlank(map.get("附属卡卡号末4位"))){
						String cardEndOfFour = map.get("附属卡卡号末4位").toString();
						bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
					}
					bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyy/MM/dd"));
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyy/MM/dd"));
					bankMonthDetailEntity.setMerchandiseDetail(dateList.get(j+2));
					bankMonthDetailEntity.setCurrencyType(dateList.get(j+3));
					bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+4))));
					bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
					bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
					j += 7;
				}
			}
		}
	}
	
	/**
	 * 获取账单类别
	 * 
	 * @param parse			原账单内容集合
	 * @param listString	截取有用的字符串集合
	 * @return	返回银行类别(1=交通银行银联标准、2=交通银行维萨标准、3=交通银行万事达卡、4=无分类)
	 */
	private int getBankBillType(List<String> parse, List<String> listString) {
		int flag = -1;
		if (CollectionUtils.isNotEmpty(parse)) {
			int size = parse.size();
			// 1. 获取交通银行模板类型
			for (int index = 0; index < size; index++) {
				if (parse.get(index).contains("交通银行银联标准")) {
					flag = 1;
					break;
				}
				if (parse.get(index).contains("交通银行维萨标准")) {
					flag = 2;
					break;
				}
				if (parse.get(index).contains("交通银行万事达卡")) {
					flag = 3;
					break;
				}
				if (index == (size-1)){ // 	TODO 目前有个模板无明显标示,暂定为无分类; 但要是遇见新模板也会走无分类逻辑,待追加新模板 ---zzj
					flag = 4; // 无分类模型
				}
			}
			// 2. 截取有用信息
			for (int k = 0; k < size; k++) {
				if (parse.get(k).contains("尊敬的") && parse.get(k).contains("您好")) { // 开始位置
					for (int index = k; index < size; index++) {
						// 详细内容的提取
						if (!parse.get(index).contains("尊敬的交通银行")) { // 结束位置
							listString.add(parse.get(index).replace("\\s", "").replace("&nbsp;", ""));
						} else {
							break;
						}
					}
				}
			}
		} else {
			flag = -1;
			log.error("交通银行模板未找到区分模板类型关键字");
		}
		
		return flag;
	}

	/**
	 * 	本期账务说明
	 * 		a)	本期账单应还款额 、 本期账单最低还款额 、 积分余额 
	 * @param listString	 截取主要内容的字符串数组
	 * @param map	以键值对的形式存放账务说明
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private Map<String, String> fetchKeyMap(List<String> listString, Map map) {
		
		int size = listString.size();
		for(int i = 0; i < size; i++){
			String string = listString.get(i);
			if (string.contains("尊敬的")){
				int indexOf = string.indexOf("您好！");
				String substring = string.substring(3, indexOf-2);
				map.put("姓名", substring);
			}
			if (string.contains("先生")){
				map.put("性别", "男");
				break;
			}else if (string.contains("女士")){
				map.put("性别", "女");
				break;
			}else{
				map.put("性别", "");
				break;
			}
		}
		
		int indexOf = listString.indexOf("本期账单应还款额StatementBalance");
		int indexOf2 = listString.indexOf("本期账单最低还款额MinimumPaymentDue");
		int indexOf3 = listString.indexOf("积分余额AvailableBonusPoints");
		
		map.put("本期账单应还款额", listString.get(indexOf+2));
		map.put("本期账单应还款额$", listString.get(indexOf+3));
		
		map.put("本期账单最低还款额", listString.get(indexOf2+3));
		map.put("本期账单最低还款额$", listString.get(indexOf2+4));
		
		map.put("积分余额", listString.get(indexOf3+2));
		map.put("积分余额$",listString.get(indexOf3+4));
		
		
		return map;
	}
	
	/**
	 * 	账户信息
	 * 		a)	账单周期 、 信用额度 、 到期还款日 、 取现额度 、还款/退货/费用返还 、 消费/取现/其他费用
	 * @param listString	 截取主要内容的字符串数组
	 * @param map	键值对的存放账户信息
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> fetchKeyMapAccount(List<String> listString, Map map) {
//		int size = listString.size();
//		for (int i = 0; i < size; i++){
//			if (listString.contains("本期积分说明BonusPointSummary")){
//				break;
//			}
			map.put("账单周期", listString.get(listString.indexOf("账单周期StatementCycle")+1));
			String value = listString.get(listString.indexOf("信用额度CreditLimit")+1);
			int index = value.indexOf("美");
			map.put("信用额度", value.substring(0, index));
			map.put("信用额度$", value.substring(index, value.length()));
			map.put("到期还款日", listString.get(listString.indexOf("到期还款日PaymentDueDay")+1));
			String value1 = listString.get(listString.indexOf("取现额度CashAdvanceLimit")+1);
			int index1 = value1.indexOf("美");
			map.put("取现额度", value1.substring(0, index1));
			map.put("取现额度$", value1.substring(index, value.length()));
			
			int indexOf = listString.indexOf("上期账单应还款额LastStatementBalance");
			int indexOf2 = listString.indexOf("还款/退货/费用返还Payment/Credit");
			int indexOf3 = listString.indexOf("消费/取现/其他费用NewSpending/Cashadvance/Charges");
			map.put("上期账单应还款额", listString.get(indexOf+7));
			map.put("上期账单应还款额$", listString.get(indexOf+12));
			map.put("还款/退货/费用返还", listString.get(indexOf2+6));
			map.put("还款/退货/费用返还$", listString.get(indexOf2+11));
			map.put("消费/取现/其他费用", listString.get(indexOf3+5));
			map.put("消费/取现/其他费用$", listString.get(indexOf3+10));
//		}
		return map;
	}
	/**
	 * 	本账户积分明细
	 * 		a)	到期积分
	 * @param listString
	 * @param map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> fetchCredits(List<String> listString, Map map) {
		int indexOf = listString.indexOf("积分余额AvailablePoints");
		// 将失效的人民币积分,分为三段
		map.put("A-RMB", listString.get(indexOf+6));
		map.put("B-RMB", listString.get(indexOf+7));
		map.put("C-RMB", listString.get(indexOf+8));
		// 将失效的美元积分,分为三段
		map.put("A-USD", listString.get(indexOf+11));
		map.put("B-USD", listString.get(indexOf+12));
		map.put("C-USD", listString.get(indexOf+13));
		// 积分失效日期,分为三段
		String value0 = listString.get(indexOf+1);
		String value2 = listString.get(indexOf+2);
		String value3 = listString.get(indexOf+3);
		// 格式: 日期[RMB:XXXDSD:YYY];...
		String key0 = value0.substring(0, 11) + "[RMB:" + map.get("A-RMB")+" USD:"+map.get("A-USD")+"]";
		String key1 = value2.substring(0, 11) + "[RMB:" + map.get("B-RMB")+" USD:"+map.get("B-USD")+"]";
		String key2 = value3.substring(0, 11) + "[RMB:" + map.get("C-RMB")+" USD:"+map.get("C-USD")+"]";
		
		map.put("到期积分", key0+";"+key1+";"+key2);
		
		return map;
	}

	/**
	 * 还款交易明细
	 * 
	 * @param detailList
	 * @param colums
	 */
	private List<List<String>> incomeDetailsList(List<String> detailList,
			int colums) {
		boolean falg1 = true;
		boolean falg2 = true;
		List<List<String>> returnList = new ArrayList<List<String>>(); // 用于存放交易明细的二维数组
		if (CollectionUtils.isNotEmpty(detailList)) {
			int length = detailList.size();
			for (int i = 0; i < length; i++) {
				if (falg1 && !detailList.get(i).contains("以下是您的还款、退货及费用返还明细")) { // 还款起始位置
					continue; // 跳过
				}
				if (falg2 && !detailList.get(i).contains("人民币账户明细")) {
					falg1 = false;
					continue;
				} else {
					falg2 = false;
					colums = 7; // 重置列数7
					if (detailList.get(i).contains("以下是您的消费、取现及其他费用明细")) { // 结束条件
						break;
					}
					if (detailList.get(i).contains("人民币账户明细")) {
						continue; // 加1前只在 "人民币账户明细"位置
					}
					if (detailList.get(i).contains("卡号末四位")) {
						continue; // 加1前只在 "主卡卡号末四位xxxx"位置
					}
					// 二维数组存放
					List<String> tempList = new ArrayList<String>(); // 临时存储数据
					for (int item = 0; item < colums; item++) {
						tempList.add(detailList.get(i++)); // 以行为单位
					}
					--i;
					returnList.add(tempList); // 存入returnList
				}
			}
		}

		return returnList;
	}

	/**
	 * 主卡消费明细 a) 交易日期 、 记账日期 、 交易说明 、 交易币种/金额 、 清算币种/金额 b) 另外还有主卡附属卡的区别
	 * 
	 * @param detailList
	 *            截取主要内容的字符串数组
	 * @param colums
	 *            交易明细的循环列数
	 * @return visa卡主卡交易明细的二维数组
	 * @author zhaozj
	 */
	private List<List<String>> masterCardDetails(List<String> detailList,
			int colums, Map<String, String> map) {

		boolean falg1 = true;
		boolean falg2 = true;
		List<List<String>> returnList = new ArrayList<List<String>>(); // 用于存放交易明细的二维数组
		if (CollectionUtils.isNotEmpty(detailList)) {
			int length = detailList.size();
			for (int i = 0; i < length; i++) {
				if (falg1 && !detailList.get(i).contains("以下是您的消费")) {
					continue; // 跳过 
				}
				if (falg2 && !detailList.get(i).contains("人民币账户明细")) {
					falg1 = false;
					continue;
				} else {
					falg2 = false;
					colums = 7; // 重置列数7
					if (detailList.get(i).contains("人民币账户明细")) {
						continue; // 加1前只在 "人民币账户明细"位置
					}
					// 解析主卡消费明细开始
					if (!detailList.get(i).contains("主卡卡号末四位")) { // 人民币账户明细字样下有多行交易明细
						// 主卡消费明细结束 条件
						if (detailList.get(i).contains("附属卡卡号末四位")
								|| detailList.get(i).contains(
										"感谢您对交通银行信用卡业务的支持")
								|| detailList.get(i).contains("本期积分说明")) {
							log.info("交通银行主卡消费明细已结束");
							break;
						}

						// 二维数组存放
						List<String> tempList = new ArrayList<String>(); // 临时存储数据
						for (int item = 0; item < colums; item++) {
							tempList.add(detailList.get(i++)); // 以行为单位
						}
						--i;
						returnList.add(tempList); // 存入returnList
					} else {
						map.put("主卡卡号末4位",
								detailList.get(i).substring(
										detailList.get(i).length() - 4,
										detailList.get(i).length()));
						continue;
					}
				}
			}
		}

		return returnList;
	}

	/**
	 * 附属卡消费明细
	 * 
	 * @param detailList
	 *            数据源
	 * @param colums
	 *            交易详细的数据列数
	 */
	private List<List<String>> supplementCardDetails(List<String> detailList,
			int colums, Map<String, String> map) {
		boolean falg1 = true;
		boolean falg2 = true;
		List<List<String>> returnList = new ArrayList<List<String>>(); // 用于存放附属卡交易详细数据
		if (CollectionUtils.isNotEmpty(detailList)) {
			int length = detailList.size();
			for (int i = 0; i < length; i++) {
				if (falg1 && !detailList.get(i).contains("以下是您的消费、取现及其他费用明细")) {
					continue; // 跳过
				}
				if (falg2 && !detailList.get(i).contains("附属卡卡号末四位")) {
					falg1 = false;
					continue;
				} else {
					falg2 = false;
					if (detailList.get(i).contains("感谢您对交通银行信用卡业务的支持")) {
						log.info("交通银行附属卡消费交易结束");
						break;
					}
					if (detailList.get(i).contains("附属卡卡号末四位")) {
						map.put("附属卡卡号末4位",
								detailList.get(i).substring(
										detailList.get(i).length() - 4,
										detailList.get(i).length()));
						continue; // 加1前只在 "附属卡卡号末四位xxxx"位置
					}
					// 二维数组存放
					List<String> tempList = new ArrayList<String>(); // 临时存储数据
					for (int item = 0; item < colums; item++) {
						tempList.add(detailList.get(i++)); // 以行为单位
					}
					--i;

					returnList.add(tempList);// 将tempList放入list内形成交易详细的二维数组
				}
			}
		}

		return returnList;
	}
}
