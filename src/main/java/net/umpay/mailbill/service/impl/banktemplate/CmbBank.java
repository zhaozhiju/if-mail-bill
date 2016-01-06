package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.umpay.mailbill.api.banktemplate.IBankTemplateService;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.service.impl.resolve.BalanceDetailServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillBankDayDetailServiceImpl;
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
 * 招商邮件账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CmbBank implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CmbBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CMB;
	}
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private BillBankDayDetailServiceImpl bankDayDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl integrationDetailServiceImpl;
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bankTemplateParse(List<String> temp, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
		
		boolean update = false;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		List<String> listString = new ArrayList<String>();	// 存储所有数据
		Map<String, String> map = new HashMap<String, String>(); // 抓取值
		int flag = 0;
		Long[] billId = null;
		
		// 判断解析模板
		flag = getBillType(temp, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CmbBank.class, flag});
		if (flag == 1){
			map.clear();
			Map<String, String> fetchKey = fetchKey(listString, map);
			List<List<String>> list = detailedContent(listString, 4);//账单交易的详细
			//循环找出几张卡
			Set<String> set = getCardNum(list, 2, 3);
			//说明有几张卡
			int size = set.size();
			//将list转化为单一卡号的交易明细
			List<List<String>>[] tempList = getListConvert(list, set, size, 2, 3);
			//开始循环存放数据
			for (int e = 0; e < size; e++){
				BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
				billCycleInfoEntity.setAccountId(accountId);
				billCycleInfoEntity.setScVersion(scVersion);
				billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
				billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
				billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CMB);//商务卡账单
				billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
				billCycleInfoEntity.setSenderUrl(senderUrl);
				billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
				billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
				billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
				billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
				billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKey.get("到期还款日")));
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKey.get("本期应还金额"))));
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(fetchKey.get("最低还款额"))));
				billCycleInfoEntity.setUsaIntegration(NumberTools.toLong(fetchKey.get("截至上月底可用积分")));
				billCycleInfoEntity.setCardEndOfFour(tempList[e].get(0).get(3));
				billCycleInfoEntity.setUserName(fetchKey.get("姓名"));
				if (StringUtils.isNotBlank(fetchKey.get("性别"))){
					billCycleInfoEntity.setUserGender(fetchKey.get("性别"));
				}else{
					billCycleInfoEntity.setUserGender(null);
				}
				String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
				if (StringUtils.isNotBlank(emailInfo)){
					Date stringToDate = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
					billCycleInfoEntity.setSentData(stringToDate);
				}
				billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
				billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
				if(update){
					billCycleInfoEntity.setId(id[e]);
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
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKey.get("本月支出"))));
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKey.get("上月结余"))));
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKey.get("本月收入"))));
				balanceDetailServiceImpl.save(balanceDetailEntity);
				
				IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
				if (update){
					billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
					integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
				}
				integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
				integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
				integrationDetailEntity.setUsePoints(NumberTools.toInt(fetchKey.get("截至上月底可用积分")));
				integrationDetailEntity.setBalancePoints(NumberTools.toInt(fetchKey.get("上期可用积分余额")));
				integrationDetailEntity.setRevisePoints(NumberTools.toInt(fetchKey.get("本期调整积分")));
				integrationDetailEntity.setExchangePoints(NumberTools.toInt(fetchKey.get("本期兑换积分总数")));
				integrationDetailEntity.setAwardPoints(NumberTools.toInt(fetchKey.get("本期奖励积分")));
				integrationDetailEntity.setAddedPoints(NumberTools.toInt(fetchKey.get("本期新增积分")));
				integrationDetailServiceImpl.save(integrationDetailEntity);
				
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
				int size2 = tempList[e].size();
				for (int i = 0; i < size2; i++){
					List<String> dateList = tempList[e].get(i);
					for (int j = 0; j < dateList.size(); ){
						BillBankMonthDetailEntity bankMonthDetailUsaEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailUsaEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailUsaEntity.setId(bankMonthDetailUsaEntity.getId());
						}
						bankMonthDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						String dateStr = dateList.get(j);
						if (dateStr.equals("&nbsp;")){
							bankMonthDetailUsaEntity.setMerchandiseDate(null);
						}else{
							bankMonthDetailUsaEntity.setMerchandiseDate(DateUtil.stringToDate(getDate(fetchKey.get("到期还款日"), dateList, -1)));
						}
						bankMonthDetailUsaEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(j+1)));
						String strInput = dateList.get(j+2);
						if (strInput.contains("-")){
							bankMonthDetailUsaEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						}
						bankMonthDetailUsaEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
						bankMonthDetailUsaEntity.setCardEndOfFour(dateList.get(j+3));
						bankMonthDetailUsaEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
						bankMonthDetailServiceImpl.save(bankMonthDetailUsaEntity);
						j += 4;
					}
				}
			}
			return "1";
		}
		// 招行商务卡账单(个人承债)
		if (flag == 2){
			map.clear();
//			log.info("---------------招商商务卡---------------");
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.汇总信息
			Map<String, String> fetchKeyMap = fetchKeyMap1(listString, map);
			// 2.抽取积分统计信息
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 3.账单交易的详细
			List<List<String>> payMentDetails = detailedContentLIst(listString, 7);
			// 3.1 TODO 需处理多卡问题
			
			// 4.账单周期表
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_MONTHLY_CMB);//商务卡账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(senderUrl);
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String dateBill = fetchKeyMap.get("账单日");
			if (StringUtils.isNotBlank(dateBill) && dateBill.length() > 2){
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(dateBill));
				Date string2DateMonth = DateUtil.string2DateMonth(dateBill);
				String formatDate = DateUtil.getFormatDate(string2DateMonth,"yyyyMM");
				billCycleInfoEntity.setAccountOfDate(formatDate);
			}
			Date paymentDueDate = null;
			if (StringUtils.isNotBlank(fetchKeyMap.get("到期还款日"))) {
				paymentDueDate = DateUtil.stringToDate(fetchKeyMap.get("到期还款日"));
				billCycleInfoEntity.setPaymentDueDate(paymentDueDate);
			}
			
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("信用额度"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(fetchCredits.get("可用积分余额")));
			String newRmbBalance = fetchKeyMap.get("本期应还金额");
			if (StringUtils.isNotBlank(newRmbBalance)){
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(newRmbBalance)));
			}
			String newUsaBalance = fetchKeyMap.get("本期应还金额$");
			if (StringUtils.isNotBlank(newUsaBalance)){
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.toNumberic(newUsaBalance)));
			}
			if(CollectionUtils.isNotEmpty(payMentDetails)){
				billCycleInfoEntity.setCardEndOfFour(payMentDetails.get(2).get(4)); // 卡号末四位
			}
			billCycleInfoEntity.setUserName(fetchKeyMap.get("姓名"));
			if (StringUtils.isNotBlank(fetchKeyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(fetchKeyMap.get("性别"));
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
			
			// 5.人民币的账单金额汇总
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期账单金额"))));
			balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期调整金额"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期账单金额"))));
			balanceDetailEntity.setInterest(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("循环利息"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期还款金额"))));
			if (StringUtils.isNotBlank(newRmbBalance)){
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(newRmbBalance)));
			}
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			// 美元的账单金额汇总
			BalanceDetailEntity balanceDetailEntityUsa = new BalanceDetailEntity();
			if (update){
				balanceDetailEntityUsa = balanceDetailServiceImpl.getId(billId[1]);
			}
			balanceDetailEntityUsa.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
			balanceDetailEntityUsa.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntityUsa.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期账单金额$"))));
			balanceDetailEntityUsa.setAdjustment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期调整金额$"))));
			balanceDetailEntityUsa.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期账单金额$"))));
			balanceDetailEntityUsa.setInterest(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("循环利息$"))));
			balanceDetailEntityUsa.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期还款金额$"))));
			if (StringUtils.isNotBlank(newUsaBalance)){
				balanceDetailEntityUsa.setNewBalance(NumberTools.createDouble(Money.toNumberic(newUsaBalance)));
			}
			balanceDetailServiceImpl.save(balanceDetailEntityUsa);
			
			// 6.积分的抓取
			if (-1 != listString.indexOf("积分统计")){
				IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
				if (update){
					billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
					integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
				}
				integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
				integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
				integrationDetailEntity.setBalancePoints(NumberTools.toInt(fetchCredits.get("上期可用积分余额")));
				integrationDetailEntity.setAddedPoints(NumberTools.toInt(fetchCredits.get("本期新增积分")));
				integrationDetailEntity.setRevisePoints(NumberTools.toInt(fetchCredits.get("本期调整积分")));
				integrationDetailEntity.setAwardPoints(NumberTools.toInt(fetchCredits.get("本期奖励积分")));
				integrationDetailEntity.setUsePoints(NumberTools.toInt(fetchCredits.get("可用积分余额")));
				integrationDetailEntity.setExchangePoints(NumberTools.toInt(fetchCredits.get("本期兑换积分总数")));
				integrationDetailServiceImpl.save(integrationDetailEntity);
			}
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			
			// 7.交易明细详情
			if (CollectionUtils.isNotEmpty(payMentDetails)) {
				String paymentDate = DateUtil.getFormatDate(paymentDueDate);
				int length = payMentDetails.size();
				for (int i = 0; i < length; i++){
					if (i <= 1){ // 列表的前两行(列表的头信息)
						continue;
					}
					List<String> rowInfo = payMentDetails.get(i);
					if(CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength; ){
							if(rowInfo.get(0).contains("交易日") || rowInfo.get(0).contains("TransDate")){ // 跳过美元交易的头信息
								continue;
							}
							// 人民币/美元交易明细
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							String dateStr = rowInfo.get(j);
							if (dateStr.equals("&nbsp;")){
								bankMonthDetailEntity.setMerchandiseDate(null);
							}else{
								String merDate = getDate(paymentDate, rowInfo, -1); // rowInfo的第一列数据
								if (StringUtils.isNotBlank(merDate)){
									bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(merDate)); // 交易日
								}
							}
							String postDate = getDate(paymentDate, rowInfo, 0); // rowInfo的第二列数据
							if (StringUtils.isNotBlank(postDate)) {
								bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(postDate)); // 记账日 
							}
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+2)); // 交易摘要
							String amount = rowInfo.get(j+3);
							if (amount.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME); // 收入/支出(负:收入;正:支出)
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(amount))); // 交易金额
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+4)); // 卡号末四位
							bankMonthDetailEntity.setMerchandiseArea(rowInfo.get(j+5)); // 交易地点 
							if (amount.startsWith("￥")) { // 人民币标示
								bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
							} else {
								bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
							}
							
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += rowLength;
						}
					} else {
						log.error("--- 招商商务银行行数据为空 ---");
					}
				}
			} else {
				log.info("--- 招商商务卡交易明细为空 ---");
			}
			
			return "1";
		}
		
		//招商信用卡消费明细
		if (flag == 3){
			// 清理缓存数据
			map.clear();
			
			getUserNameAndSex(listString, map);
			List<List<String>> list = dayDetailedContent(listString, 6);//账单交易的详细
			//循环找出几张卡
			Set<String> set = getCardNum(list, 0, 0);
			//说明有几张卡
			int size = set.size();
			//将list转化为单一卡号的交易明细
			List<List<String>>[] tempList = getListConvert(list, set, size, 0, 0);
			for(int n = 0; n < size; n++){
				BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
				billCycleInfoEntity.setAccountId(accountId);
				billCycleInfoEntity.setScVersion(scVersion);
				billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
				billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_DAY);//日账单
				billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_DAY_CMB);//消费明细的账单
				billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
				billCycleInfoEntity.setSenderUrl(senderUrl);
				billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
				billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
				billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
				billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
				billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
				int size2 = tempList[n].size();
				if (size2 != 0){
					String accountOfDate = tempList[n].get(0).get(1);
					if (accountOfDate.indexOf("/") != -1){
						accountOfDate = "20"+accountOfDate;
						accountOfDate = accountOfDate.replaceAll("/", "");
						billCycleInfoEntity.setAccountOfDate(accountOfDate);
					}else{
						billCycleInfoEntity.setAccountOfDate(accountOfDate);
					}
				}
				billCycleInfoEntity.setCardEndOfFour(size2 == 0 ? "" : tempList[n].get(0).get(0));
				billCycleInfoEntity.setUserName(map.get("姓名"));
				if (StringUtils.isNotBlank(map.get("性别"))){
					billCycleInfoEntity.setUserGender(map.get("性别"));
				}else{
					billCycleInfoEntity.setUserGender(null);
				}
				String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
				if (StringUtils.isNotBlank(emailInfo)){
					Date stringToDate = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
					billCycleInfoEntity.setSentData(stringToDate);
				}
				billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
				billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
				if(update){
					billCycleInfoEntity.setId(id[n]);
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}else{
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}
				
				billJobServiceImpl.saveJob(billCycleInfoEntity);
				
				for (int i = 0; i < size2; i++){
					List<String> dateList = tempList[n].get(i);
					for (int j = 0; j < dateList.size(); ){
						BillBankDayDetailEntity bankDayDetailEntity = new BillBankDayDetailEntity();
						bankDayDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankDayDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						bankDayDetailEntity.setCardEndOfFour(dateList.get(j));
						String dateStr = dateList.get(j+1)+" "+dateList.get(j+2);
						dateStr = dateStr.indexOf("/") != -1 ? "20"+dateStr : dateStr;
						bankDayDetailEntity.setMerchandiseDate(DateUtil.stringToDateTime(dateStr));
						bankDayDetailEntity.setMerchandiseTime(DateUtil.stringToDateTime(dateStr));
						bankDayDetailEntity.setCurrencyType(dateList.get(j+3));
						String strInput = dateList.get(dateStr.indexOf("/") != -1 ? j+4 : j+5);
						if (strInput.contains("-")){
							bankDayDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						}
						bankDayDetailEntity.setMerchandiseAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
						bankDayDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(dateStr.indexOf("/") != -1 ? j+5 : j+4)));
						bankDayDetailServiceImpl.save(bankDayDetailEntity);
						j += 6;
					}
				}
			}
			return "1";
		}
		
		//招行个人信用卡新版账单
		if (flag == 4){
			// 清理缓存数据
			map.clear();
						
			Map<String, String> fetchKeyMap2 = fetchKeyMap2(listString, map);
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_NEW_CMB);//招行新版账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(senderUrl);
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String str = fetchKeyMap2.get("账期");
			if (StringUtils.isNotBlank(str)){
				billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate( DateUtil.string2DateMonth(str),"yyyyMM"));
			}
			if (StringUtils.isNotBlank(fetchKeyMap2.get("最后还款日"))){
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMap2.get("最后还款日")));
			}
			String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
			if (StringUtils.isNotBlank(emailInfo)){
				Date stringToDate = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
				billCycleInfoEntity.setSentData(stringToDate);
			}
			billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
			billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap2.get("本期应还金额"))));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap2.get("最低还款额"))));
			billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap2.get("本期应还金额$"))));
			billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap2.get("最低还款额$"))));
			billCycleInfoEntity.setUserName(map.get("姓名"));
			if (StringUtils.isNotBlank(map.get("性别"))){
				billCycleInfoEntity.setUserGender(map.get("性别"));
			}else{
				billCycleInfoEntity.setUserGender(null);
			}
			if(update){
				billCycleInfoEntity.setId(id[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			return "1";
		}
		
		//招行个人卡旧版账单
		if (flag == 5){
			// 清理缓存数据
			map.clear();
						
			//邮件头部主要信息的抓取----账单周期
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);	
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			List<List<String>> list = oldDetailedContentLIst(listString, 7);//账单详细
			//循环找出几张卡，是否是多卡
			Set<String> set = getCardNum(list, 2, 4);
			//说明有几张卡的标示
			int size = set.size();
			String endFour[] = new String[size];
			@SuppressWarnings("unchecked")
			List<List<String>> tempList[] = new ArrayList[size];//存放单张卡明细
			int m = 0;
			for (String str : set) {
				endFour[m] = str;
				m++;
			}
			m = 0;
			//是否双币
			List<List<String>> listUSA = new ArrayList<List<String>>();
			List<List<String>> listRMB = new ArrayList<List<String>>();
			int size2 = list.size();
			for (int k = 0; k < size2; k++){
				if (list.get(k).size() == 1){
					for (int p = k ; p < size2; p++){
						listUSA.add(list.get(p));
					}
					break;
				}
				listRMB.add(list.get(k));
			}
			
			//多卡多币种
			int size4 = listUSA.size();
			if (size4 != 0 && size != 1){
				for (int  i = 0 ; i < size; i ++){
					tempList[i] = new ArrayList<List<String>>();
				}
				//人民币账户信息
				int size3 = listRMB.size();
				for (int j = 2 ; j < size3; j ++){
					String string = listRMB.get(j).get(4).toString();
					for (int i = 0; i < size; i++){
						if (endFour[i].equals(string)){
							tempList[i].add(listRMB.get(j));
							continue;
						}
					}
				}
				
				for (int j = 1 ; j < size4; j ++){
					String string = listUSA.get(j).get(4).toString();
					for (int i = 0; i < size; i++){
						if (endFour[i].equals(string)){
							if (j == 1){
								tempList[i].add(listUSA.get(0));
							}
							tempList[i].add(listUSA.get(j));
						}
					}
				}
			}else if (size == 1){		//单卡单币种	//单卡多币种
				//创建x个list存放
				for (int  i = 0 ; i < size; i ++){
					tempList[i] = new ArrayList<List<String>>();
				}
				for (int i = 2; i < size2; i++){
					tempList[0].add(list.get(i));
				}
			}else {		//多卡单币种
				//创建x个list存放
				for (int  i = 0 ; i < size; i ++){
					tempList[i] = new ArrayList<List<String>>();
				}
				for (int j = 2 ; j < size2; j ++){
					if(list.get(j).size() == 1){
						continue;
					}
					String string = list.get(j).get(4).toString();
					for (int i = 0; i < size; i++){
						if (endFour[i].equals(string)){
							tempList[i].add(list.get(j));
						}
					}
				}
			}
			for (int e = 0; e < size; e++){
				BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
				billCycleInfoEntity.setAccountId(accountId);
				billCycleInfoEntity.setScVersion(scVersion);
				billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
				billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
				billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_OLD_CMB);//招行旧版账单
				billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
				billCycleInfoEntity.setSenderUrl(senderUrl);
				billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
				billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
				billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
				billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
				billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
				String string = fetchKeyMap.get("账单周期");
				String[] split = string.split("-");
				if (split.length!=0){
					billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
					Date stringToDate = DateUtil.stringToDate(split[1]);
					billCycleInfoEntity.setBillCycleEnd(stringToDate);// 周期的拆分
					billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));
					String formatDate = DateUtil.getFormatDate(stringToDate,"yyyyMM");
					billCycleInfoEntity.setAccountOfDate(formatDate);
				}
				Date paymentDate = DateUtil.stringToDate(fetchKeyMap.get("到期还款日"));
				billCycleInfoEntity.setPaymentDueDate(paymentDate);
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.toNumberic(fetchKeyMap.get("预借现金额度"))));
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("信用额度"))));
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(fetchCredits.get("可用积分余额")));
				if (StringUtils.isNotBlank(fetchKeyMap.get("人民币本期应还金额"))){
					billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("人民币本期应还金额"))));
				}else if (StringUtils.isNotBlank(fetchKeyMap.get("本期应还金额"))){
					billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还金额"))));
				}
				if (StringUtils.isNotBlank(fetchKeyMap.get("美元本期应还金额"))){
					billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("美元本期应还金额"))));
				}else if (StringUtils.isNotBlank(fetchKeyMap.get("本期应还金额$"))){
					billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还金额$"))));
				}
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期最低还款额"))));
				if (StringUtils.isNotBlank(fetchKeyMap.get("本期最低还款额$"))){
					billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期最低还款额$"))));
				}
				List<String> list2 = tempList[e].get(0);
				billCycleInfoEntity.setCardEndOfFour(list2.size() == 1 ? tempList[e].get(1).get(4) : list2.get(4));
				billCycleInfoEntity.setUserName(map.get("姓名"));
				if (StringUtils.isNotBlank(map.get("性别"))){
					billCycleInfoEntity.setUserGender(map.get("性别"));
				}else{
					billCycleInfoEntity.setUserGender(null);
				}
				String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
				if (StringUtils.isNotBlank(emailInfo)){
					Date stringToDate1 = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
					billCycleInfoEntity.setSentData(stringToDate1);
				}
				billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
				billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
				if(update){
					billCycleInfoEntity.setId(id[e]);
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
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期账单金额"))));
				balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期调整金额"))));
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期账单金额"))));
				balanceDetailEntity.setInterest(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("循环利息"))));
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期还款金额"))));
				if (StringUtils.isNotBlank(fetchKeyMap.get("人民币本期应还金额"))){
					balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("人民币本期应还金额"))));
				}else if (StringUtils.isNotBlank(fetchKeyMap.get("本期应还金额"))){
					balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还金额"))));
				}
				balanceDetailServiceImpl.save(balanceDetailEntity);
				
				if (StringUtils.isNotBlank(fetchKeyMap.get("上期账单金额$"))){
					BalanceDetailEntity balanceDetailUsaEntity = new BalanceDetailEntity();
					if (update){
						balanceDetailUsaEntity = balanceDetailServiceImpl.getId(billId[1]);
					}
					balanceDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
					balanceDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					balanceDetailUsaEntity.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期账单金额$"))));
					balanceDetailUsaEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期调整金额$"))));
					balanceDetailUsaEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期账单金额$"))));
					balanceDetailUsaEntity.setInterest(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("循环利息$"))));
					balanceDetailUsaEntity.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("上期还款金额$"))));
					if (StringUtils.isNotBlank(fetchKeyMap.get("美元本期应还金额"))){
						balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("美元本期应还金额"))));
					}else if (StringUtils.isNotBlank(fetchKeyMap.get("本期应还金额$"))){
						balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还金额$"))));
					}
					balanceDetailServiceImpl.save(balanceDetailUsaEntity);
				}
				
				//积分的抓取
				if (-1 != listString.indexOf("积分统计")){
					IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
					if (update){
						billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
						integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
					}
					integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
					integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					integrationDetailEntity.setBalancePoints(NumberTools.toInt(fetchCredits.get("上期可用积分余额")));
					integrationDetailEntity.setAddedPoints(NumberTools.toInt(fetchCredits.get("本期新增积分")));
					integrationDetailEntity.setRevisePoints(NumberTools.toInt(fetchCredits.get("本期调整积分")));
					integrationDetailEntity.setAwardPoints(NumberTools.toInt(fetchCredits.get("本期奖励积分")));
					integrationDetailEntity.setUsePoints(NumberTools.toInt(fetchCredits.get("可用积分余额")));
					integrationDetailEntity.setExchangePoints(NumberTools.toInt(fetchCredits.get("本期兑换积分总数")));
					integrationDetailServiceImpl.save(integrationDetailEntity);
				}
				//分期推荐信息
				if (-1 != listString.indexOf("推荐分期金额")){
					fetchKeyMap3(listString, map);  
				}
				if (update){
					billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
				}
				String localeString = DateUtil.getFormatDate(paymentDate);
				int f = 0;
				int size3 = tempList[e].size();
				for (int i = 0; i < size3; i++){
					List<String> dateList = tempList[e].get(i);
					if (dateList.size() == 1){
						f = 1;
						continue;
					}
					if (f == 0){
						for (int j = 0; j < dateList.size(); ){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							String dateStr = dateList.get(j);
							if (dateStr.equals("&nbsp;")){
								bankMonthDetailEntity.setMerchandiseDate(null);
							}else{
								bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(getDate(localeString, dateList, -1)));
							}
							String dateStr2 = getDate(localeString, dateList, j);
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateStr2));
							bankMonthDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(j+2)));
							String strInput = dateList.get(j+3);
							if (strInput.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
							bankMonthDetailEntity.setCardEndOfFour(dateList.get(j+4));
							if (!dateList.get(j+5).equals("&nbsp;"))
							bankMonthDetailEntity.setMerchandiseArea(dateList.get(j+5));
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
							if (!dateList.get(j+6).equals("&nbsp;"))
								bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(j+6))));
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += 7;
						}
					}
					if (f == 1 && 0 != dateList.size()){
						for (int k = 0; k < dateList.size();){
							BillBankMonthDetailEntity bankMonthDetailUsaEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailUsaEntity = bankMonthDetailServiceImpl.getId(billId[i+k]);
								bankMonthDetailUsaEntity.setId(bankMonthDetailUsaEntity.getId());
							}
							bankMonthDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							String dateStr = dateList.get(k);
							if (dateStr.equals("&nbsp;")){
								bankMonthDetailUsaEntity.setMerchandiseDate(null);
							}else{
								bankMonthDetailUsaEntity.setMerchandiseDate(DateUtil.stringToDate(getDate(localeString, dateList, -1)));
							}
							String dateStr2 = getDate(localeString, dateList, k);
							bankMonthDetailUsaEntity.setPostDate(DateUtil.stringToDate(dateStr2));
							bankMonthDetailUsaEntity.setMerchandiseDetail(dateList.get(k+2).replaceAll("&nbsp;", ""));
							String strInput = dateList.get(k+3);
							if (strInput.contains("-")){
								bankMonthDetailUsaEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailUsaEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
							bankMonthDetailUsaEntity.setCardEndOfFour(dateList.get(k+4));
							bankMonthDetailUsaEntity.setMerchandiseArea(dateList.get(k+5));
							bankMonthDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
							bankMonthDetailUsaEntity.setOriginalTransAmount(NumberTools.createDouble(Money.toNumberic(dateList.get(k+6))));
							bankMonthDetailServiceImpl.save(bankMonthDetailUsaEntity);
							k += 7;
						}
					}
				}
			}
			return "1";
		}
		//招商信用卡消费明细
		if (flag == 6){
			// 清理缓存数据
			map.clear();
						
			getNewUserNameAndSex(listString, map);
			List<List<String>> list = detailedContentDay(listString, 6);//账单交易的详细
			//循环找出几张卡
			Set<String> set = getCardNum(list, 0, 0);
			//说明有几张卡
			int size = set.size();
			//将list转化为单一卡号的交易明细
			List<List<String>>[] tempList = getListConvert(list, set, size, 0, 0);
			for(int n = 0; n < size; n++){
				BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
				billCycleInfoEntity.setAccountId(accountId);
				billCycleInfoEntity.setScVersion(scVersion);
				billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMB);
				billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_DAY);//日账单
				billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_DAY_CMB);//消费明细的账单
				billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
				billCycleInfoEntity.setSenderUrl(senderUrl);
				billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
				billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
				billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
				billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
				billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
				int size2 = tempList[n].size();
				if (size2 != 0){
					String accountOfDate = tempList[n].get(0).get(1);
					if (accountOfDate.indexOf("/") != -1){
						accountOfDate = "20"+accountOfDate;
						accountOfDate = accountOfDate.replaceAll("/", "");
						billCycleInfoEntity.setAccountOfDate(accountOfDate);
					}else{
						billCycleInfoEntity.setAccountOfDate(accountOfDate);
					}
				}
				billCycleInfoEntity.setCardEndOfFour(size2 == 0 ? "" : tempList[n].get(0).get(0));
				billCycleInfoEntity.setUserName(map.get("姓名"));
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(map.get("可用额度"))));
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.toNumberic(map.get("取现额度"))));
				if (StringUtils.isNotBlank(map.get("性别"))){
					billCycleInfoEntity.setUserGender(map.get("性别"));
				}else{
					billCycleInfoEntity.setUserGender(null);
				}
				String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
				if (StringUtils.isNotBlank(emailInfo)){
					Date stringToDate = DateUtil.stringToDate(emailInfo, DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
					billCycleInfoEntity.setSentData(stringToDate);
				}
				billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
				billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
				if(update){
					billCycleInfoEntity.setId(id[n]);
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}else{
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}
				
				billJobServiceImpl.saveJob(billCycleInfoEntity);
				
				for (int i = 0; i < size2; i++){
					List<String> dateList = tempList[n].get(i);
					for (int j = 0; j < dateList.size(); ){
						BillBankDayDetailEntity bankDayDetailEntity = new BillBankDayDetailEntity();
						bankDayDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankDayDetailEntity.setCardEndOfFour(dateList.get(j));
						String dateStr = dateList.get(j+1)+" "+dateList.get(j+2);
						dateStr = dateStr.indexOf("/") != -1 ? "20"+dateStr : dateStr;
						bankDayDetailEntity.setMerchandiseDate(DateUtil.stringToDateTime(dateStr));
						bankDayDetailEntity.setMerchandiseTime(DateUtil.stringToDateTime(dateStr));
						bankDayDetailEntity.setCurrencyType(dateList.get(j+3));
						String strInput = dateList.get(dateStr.indexOf("/") != -1 ? j+4 : j+5);
						if (strInput.contains("-")){
							bankDayDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						}
						bankDayDetailEntity.setMerchandiseAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
						bankDayDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(dateStr.indexOf("/") != -1 ? j+5 : j+4)));
						bankDayDetailServiceImpl.save(bankDayDetailEntity);
						j += 6;
					}
				}
			}
			return "1";
		}
		return "00";
	}

	/**
	 * 将各个卡号信息分别存储
	 * @param list	详细交易
	 * @param set	存放卡号信息
	 * @param size	卡号的数量
	 * @param card	卡号的位置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<List<String>>[] getListConvert(List<List<String>> list,
			Set<String> set, int size, int start, int card) {
		List<List<String>> tempList[] = new ArrayList[size];//存放单张卡明细
		String endFour[] = new String[size];
		int m = 0 ;
		for (String str : set) {
			endFour[m] = str;
			m++;
		}
		//创建x个list存放
		for (int  i = 0 ; i < size; i ++){
			tempList[i] = new ArrayList<List<String>>();
		}
		int size2 = list.size();
		if (size >= 2){
			for (int j = 0 ; j < size2; j ++){
				String string = list.get(j).get(card);
				for (int i = 0; i < size; i++){
					if (endFour[i].equals(string)){
						tempList[i].add(list.get(j));
					}
				}
			}
			
		}else{
			for (int i = start; i < size2; i++){
				tempList[0].add(list.get(i));
			}
		}
		return tempList;
	}

	/**
	 *  循环找出几张卡
	 * @param list	数据交易详细
	 * @param star 	数组循环的开始
	 * @param card	卡号末四位的位置
	 * @return	卡号的集合
	 */
	private Set<String> getCardNum(List<List<String>> list, int star, int card) {
		Set<String> set = new HashSet<String>();
		int size = list.size();
		for (int i = star; i < size; i++){
			if (list.get(i).size() == 1){
				continue;
			}
			set.add(list.get(i).get(card));
		}
		return set;
	}

	/**
	 * 截取交易详细
	 * 
	 * @param listString  具体的交易信息
	 * @param i
	 * @return  详细交易的二维数组
	 */
	private List<List<String>> detailedContent(List<String> listString, int length) {
		List<List<String>> list = new ArrayList<List<String>>();
		int k = 0;
		int size = listString.size();
		for (int i = 0; i < size; i++){
			if (listString.get(i).contains("截至上月底可用积分")){
				for (int j = i ; j < size; j++){
					if (k == 0){
						k ++;
						j += 10;
					}
					if (listString.get(j).equals("&nbsp;&nbsp;")){
						break;
					}
					List<String> tempList1 = new ArrayList<String>();
					for(int a = 0; a < length; a++){
						int cou = j + a;
						tempList1.add(listString.get(cou));
					}
					j += length - 1;
					list.add(tempList1);
				}
				break;
			}
		}
		return list;
	}

	/**
	 * 抓取值 
	 * 		到期还款日 、 上月结余、本月支出、本月收入、本期应还金额、最低还款额、截至上月底可用积分、上期可用积分余额、本期新增积分、本期调整积分、本期奖励积分、本期兑换积分总数
	 * @param listString 获取截取后的值
	 * @param map	装值
	 * @return map
	 */
	private Map<String, String> fetchKey(List<String> listString, Map<String,String> map) {
		
		//抓取用户姓名与性别
		getNameAndSex(listString, map);
		
		int indexOf = listString.indexOf("您的到期还款日为");
		map.put("到期还款日", listString.get(indexOf+1));
		map.put("上月结余", listString.get(indexOf+3));
		map.put("本月支出", listString.get(indexOf+4));
		map.put("本月收入", listString.get(indexOf+5));
		map.put("本期应还金额", listString.get(indexOf+6));
		map.put("最低还款额", listString.get(indexOf+7));
		
		int size = listString.size();
		for (int i = 0; i < size; i++){
			if (listString.get(i).contains("截至上月底可用积分")){
				for (int j = i; j < size;){
					map.put("截至上月底可用积分", listString.get(j+1));
					map.put("上期可用积分余额", listString.get(j+2));
					map.put("本期新增积分", listString.get(j+3));
					map.put("本期调整积分", listString.get(j+4));
					map.put("本期奖励积分", listString.get(j+5));
					map.put("本期兑换积分总数", listString.get(j+6));
					break;
				}
				break;
			}
		}
		return map;
	}

	/**
	 * 将现有的0903等日期补全为yyyy年mm月dd日
	 * 
	 * @param formatDate
	 * @param rowInfo
	 * @param j
	 * @return	
	 * 
	 * TODO 涉及到跨年的账单,不能这么截取账单日期的年份,需优化
	 */
	private String getDate(String formatDate, List<String> rowInfo, int j) {
//		log.info("postDate:{} begin", new Object[]{formatDate});
		String yea = formatDate.substring(0, 4);
		String postDate = rowInfo.get(j+1);
		String month = postDate.substring(0, 2);
		String day = postDate.substring(2, 4);
		postDate = yea+"年"+month+"月"+day+"日";
//		log.info("postDate:{} end", new Object[]{postDate});
		return postDate;
	}

	private int getBillType(List<String> temp, List<String> listString, int flag) {
		String replace;
		int k = 0;
		int size = temp.size();
		for (int ii = 0; ii < size; ii++){
			if (temp.get(ii).contains("尊敬的") && k == 0){
				for (int l = ii; l < size; l++){
					replace = temp.get(l).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if (replace.contains("您好！")){
						k = 1;
						break;
					}
					if (replace.contains("您好，")){
						k = 1;
						break;
					}
				}
				continue;
			}
			if (listString.size() == 0  && k == 0){
				if (temp.get(ii).contains("您好！")){
					replace = temp.get(ii).replace("\\s", "").replace("&nbsp;", "");
					listString.add(replace);
					k = 1;
					continue;
				}
			}
			
			// TODO 需要优化项, 双层循环效率会低好多...
			
			/** 招商银行信用卡电子账单（招行个人卡旧版账单）    */
			flag = oldMerchantsDetail(listString, flag, ii, temp);
			/** 招行个人信用卡新版账单      ------招商银行信用卡电子账单-10元抢兑最高价值4388元豪礼*/
			flag = newMerchantsCard(listString, flag, ii, temp);
			/** 招行商务卡账单 */
			flag = businessCard(listString, flag, ii, temp);
			/** 信用卡消费明细  		----招商银行信用卡消费明细-掌上生活推荐有礼！（招行个人信用卡每日账单——美元消费） */
			flag = masterCardDetail(listString, flag, ii, temp);
			/** 招商银行信用卡电子账单		----招商银行信用卡电子账单 ======= 您的到期还款日为 2013/06/08 */
			flag = merchantsCardDetail(listString, flag, ii, temp);
			/** 消费提醒全面升级-每日信用管家供您轻松对账,乐享信息!		----招商银行信用卡电子账单 */
			flag = merchantsDayCardDetail(listString, flag, ii, temp);
			if (flag != 0){
				return flag;
			}
		}
		return flag;
	}

		/**
		 * 招行普通
		 * 提取招商交易详细信息   
		 * 列信息:交易日, 记账日, 交易摘要, 人民币金额, 卡号末四位, 交易地点, 交易地金额
		 * 
		 * 	从‘交易日’或‘卡号末4位’开始截取我们想要的数据，放到一个二维数组里。
		 * 	其中如果遇到‘美元账户’或者‘★’,则根据账单结束其之后的数据，
		 * 	但是遇到‘美元账户’则可能是双币种账户，则开始从‘交易日’开始截取其详细内容到‘★’或者‘[备注]’结束
		 * @param listString
		 * @return
		 */
		private List<List<String>> detailedContentLIst(List<String> listString, int colums) {
			List<List<String>> list = new ArrayList<List<String>>(); // 用于存放交易详细数据
			int size2 = listString.size();
			for(int ji = 0; ji < size2; ji++){
				if (listString.get(ji).equals("交易日") || listString.get(ji).equals("卡号末4位")){ // 截取人民币账户的值
					int siz = size2-colums;
					for (int ij = ji; ij < siz; ij++){
						if (listString.get(ij).startsWith("美元账户") || listString.get(ij).equals("★")){
							System.out.println("人民币账户消费及详细内容List已完结！");
							if (listString.get(ij).startsWith("美元账户")){
								for(int j = ij; j < size2; j++){
									if (listString.get(j).equals("交易日")){
										for (int i = j ; i < size2; i++){
											if (listString.get(i).equals("★") || listString.get(i).equals("[备注]") 
													|| listString.get(i).startsWith("上述")){
												System.out.println("美元账户消费及详细内容List已完结！");
												break;
											}
											List<String> rowInfo = new ArrayList<String>();
											for(int a = 0; a < colums; a++){
												int cou = i + a;
												rowInfo.add(listString.get(cou));
											}
											i += colums - 1;
											list.add(rowInfo);
										}
										break;
									}
								}
							} 
							break;
						}
						List<String> tempList = new ArrayList<String>(); // 临时存储数据
						for(int a = 0; a < colums; a++){
							int cou = ij + a;
							tempList.add(listString.get(cou));
						}
						ij += colums - 1;
						list.add(tempList);
						if (listString.get(ij+1).equals("&nbsp;") && listString.get(ij+2).contains("立即申请")){
							break;
						}
					}
					break;
				} 
			}
			return list;
		}
		
		/**
		 * 抓取招商日账单交易明细
		 * @param listString
		 * @param size
		 * @return
		 */
		private List<List<String>> dayDetailedContent(List<String> listString, int size) {
			List<List<String>> list = new ArrayList<List<String>>(); 	//用于存放交易详细数据
			int size2 = listString.size();
			for(int i = 0; i < size2; i++){
				if (listString.get(i).equals("卡号末4位")){//截取人民币账户的值
					i += 12;
					for (int j = i; j < size2; j++){
						List<String> tempList = new ArrayList<String>();//临时存储数据
						for(int a = 0; a < size; a++){
							int cou = j + a;
							tempList.add(listString.get(cou));
						}
						j += size - 1;
						list.add(tempList);
						if (j + 1 >= size2|| listString.get(j+1).equals("&nbsp;") && listString.get(j+2).contains("立即申请") || listString.get(j).equals("★") || listString.get(j).equals("[备注]") 
								|| listString.get(j).startsWith("上述")){
							break;
						}
					}
					break;
				} 
			}
			return list;
		}
		
		/**
		 * 抓取招商新的日账单交易明细获取
		 * @param listString
		 * @param size
		 * @return
		 */
		private List<List<String>> detailedContentDay(List<String> listString, int size) {
			List<List<String>> list = new ArrayList<List<String>>(); 	//用于存放交易详细数据
			int size2 = listString.size();
			for(int i = 0; i < size2; i++){
				if (listString.get(i).contains("可用额度")){//截取人民币账户的值
					i += 4;
					for (int j = i; j < size2; j++){
						List<String> tempList = new ArrayList<String>();//临时存储数据
						for(int a = 0; a < size; a++){
							int cou = j + a;
							tempList.add(listString.get(cou));
						}
						j += size - 1;
						list.add(tempList);
						if (j + size >= size2 || listString.get(j+1).equals("&nbsp;") && listString.get(j+2).equals("&nbsp;")){
							break;
						}
					}
					break;
				} 
			}
			return list;
		}
		
		/**
		 * 招行旧版
		 * 提取招商交易详细信息   
		 * 
		 * 	从‘交易日’或‘卡号末4位’开始截取我们想要的数据，放到一个二维数组里。
		 * 	其中如果遇到‘★’,则根据账单结束其之后的数据，
		 * 	则开始从‘交易日’开始截取其详细内容到‘★’或者‘[备注]’结束
		 * @param listString
		 * @return
		 */
		private List<List<String>> oldDetailedContentLIst(List<String> listString, int size) {
			List<List<String>> list = new ArrayList<List<String>>(); 	//用于存放交易详细数据
			int size2 = listString.size();
			for(int ji = 0; ji < size2; ji++){
				if (listString.get(ji).equals("交易日")){//截取人民币账户的值
					int siz = size2-ji;
					for (int ij = ji; ij < siz; ij++){
						if (listString.get(ij).startsWith("美元账户") || listString.get(ij).startsWith("上述交易") || listString.get(ij).equals("★") 
								|| (listString.get(ij).equals("&nbsp;") && listString.get(ij+1).equals("★"))){
							System.out.println("人民币账户消费及详细内容List已完结！");
							if (listString.get(ij).startsWith("美元账户")){
								for(int j = ij; j < size2; j++){
									if (listString.get(j).equals("交易日")){
										List<String> tempList = new ArrayList<String>();
										tempList.add("交易");
										list.add(tempList);
										j += size*2;
										int k = size2-size;
										for (int i = j ; i < k; i++){
											if (listString.get(i).equals("★") || (listString.get(i).equals("&nbsp;") && listString.get(i+1).equals("★"))
													|| listString.get(i).equals("[备注]") || listString.get(i).startsWith("上述")){
												System.out.println("美元账户消费及详细内容List已完结！");
												break;
											}
											List<String> tempList1 = new ArrayList<String>();
											for(int a = 0; a < size; a++){
												int cou = i + a;
												tempList1.add(listString.get(cou));
											}
											i += size - 1;
											list.add(tempList1);
										}
										break;
									}
								}
							} 
							break;
						}
						List<String> tempList = new ArrayList<String>();//临时存储数据
						for(int a = 0; a < size; a++){
							int cou = ij + a;
							tempList.add(listString.get(cou));
						}
						ij += size - 1;
						list.add(tempList);
					}
					break;
				} 
			}
			return list;
		}
		
		/**
		 * 招商银行信用卡消费明细  	
		 * 	
		 * 			----招商银行信用卡消费明细-掌上生活推荐有礼！（招行个人信用卡每日账单——美元消费） 
		 * 
		 * @param listString---装载我们需要的数据
		 * @param flag----标示为哪一类信用卡
		 * @param ii---数组下标
		 * @param lines---所有的内容
		 * @return
		 */
		private int masterCardDetail(List<String> listString, int flag,
				int ii, List<String> lines) {
			String replace;
			if (lines.get(ii).contains("卡号末4位")){
				int size = lines.size();
				for (int jj = ii; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "");
					if (replace.startsWith("[备注]")){		//遇到“【备注】”时结束
						flag = 3;
						break;
					}
					if (!replace.equals("") && replace!=null){		//详细内容的提取
						listString.add(replace); 		//将提取出来的数据添加到listString里
					}
				}
				flag = 3;
			}
			return flag;
		}

		/**
		 * 招行商务卡账单
		 * 
		 * @param listString---装载我们需要的数据
		 * @param flag
		 * @param ii
		 * @return
		 */
		private int businessCard(List<String> listString, int flag, int ii, List<String> lines) {
			String replace;
			if (lines.get(ii).startsWith("账单日")){
				int size = lines.size();
				for (int jj = ii; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "");		
					if (!replace.equals("") && replace!=null){			//详细内容的提取
						listString.add(replace); 		//将需要的数据添加到listString里
					}
				}
				flag = 2;
			}
			return flag;
		}

		/**
		 * 招行个人卡旧版账单
		 * 
		 * 			------招行个人卡旧版账单
		 * @param listString
		 * @param flag
		 * @param ii
		 * @return
		 */
		private int oldMerchantsDetail(List<String> listString, int flag, int ii, List<String> lines) {
			String replace;
			if (lines.get(ii).contains("账单周期")){	//从人民币种开始截取想要的详细账单
				int size = lines.size();
				for (int jj = ii; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "");
					//详细内容的提取
					if (!replace.equals("") && replace!=null){
						listString.add(replace); 		//将需要的数据添加到listString里
					}
				}
				flag = 5;
			}
			return flag;
		}
		
		/**
		 * 招商银行信用卡电子账单
		 * 
		 * 			------招商银行信用卡电子账单 ======= 您的到期还款日为 2013/06/08 
		 * @param listString
		 * @param flag
		 * @param ii
		 * @return
		 */
		private int merchantsCardDetail(List<String> listString, int flag, int ii, List<String> lines) {
			String replace;
			int size = lines.size();
			for (int k = 0; k < size; k++){
				if (lines.get(ii).contains("您的到期还款日为")){	//从您的到期还款日为开始截取想要的详细账单
					for (int jj = ii; jj < size; jj++){
						replace = lines.get(jj).replace("\\s", "");
						//详细内容的提取
						if (StringUtils.isNotBlank(replace)){
							listString.add(replace); 		//将需要的数据添加到listString里
						}
					}
					flag = 1;
				}
			}
			return flag;
		}
		
		/**
		 * 消费提醒全面升级-每日信用管家供您轻松对账,乐享信息!
		 * 
		 * 			------招商银行信用卡电子账单
		 * @param listString
		 * @param flag
		 * @param ii
		 * @return
		 */
		private int merchantsDayCardDetail(List<String> listString, int flag, int ii, List<String> lines) {
			String replace;
			int size = lines.size();
			//for (int k = 0; k < size; k++){
				if (lines.get(ii).contains("您的信用额度如下")){	//从'您的信用额度如下'开始截取想要的详细账单
					for (int jj = ii; jj < size; jj++){
						replace = lines.get(jj).replace("\\s", "");
						//详细内容的提取
						if (StringUtils.isNotBlank(replace)){
							listString.add(replace); 		//将需要的数据添加到listString里
						}
						if (replace.contains("分期最高送")){
							break;
						}
					}
					flag = 6;
				}
			//}
			return flag;
		}

		/**
		 * 招行个人信用卡新版账单
		 * 
		 * -----招商银行信用卡电子账单-10元抢兑最高价值4388元豪礼！（招行个人信用卡新版账单）
		 * @param listString
		 * @param ii
		 */
		private int newMerchantsCard(List<String> listString, int flag, int ii, List<String> lines) {
			String replace;
			if (lines.get(ii).contains("人民币交易")){	
				int size = lines.size();
				for(int i = 0; i < size; i++){
					if (lines.get(i).contains("尊敬的")){
						for (int j = i; j < size; j++){
							listString.add(lines.get(j));
							if (lines.get(j).equals("人民币交易")){
								break;
							}
						}
						break;
					}
				}
				//从人民币种开始截取想要的详细账单
				for (int jj = ii; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "");
					if (!replace.equals("") && replace!=null){ 		//详细内容的提取
						listString.add(replace); 		//将需要的数据添加到listString里
					}
					if (replace.equals("★")){			//遇到“★”时结束
						System.out.println("信用卡消费明细！");
						break;
					}
				}
				flag = 4;
			}
			return flag;
		}

		/**
		 * 招商银行信用卡电子账单
		 * 				-----招商银行信用卡电子账单
		 * 账单周期  、到期还款日  、 信用额度  、本期还款总额 、  预借现金额度 、 本期最低还款额 
		 * 放入map中
		 * @param listString
		 * @param map
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map<String, String> fetchKeyMap(List<String> listString, Map map) {
			//抓取用户姓名与性别
			getUserNameAndSex(listString, map);
			//抓取值后分类 
			String cyclValue = listString.get(listString.indexOf("账单周期")+2);
			int indexOf = cyclValue.indexOf("：");
			if(indexOf != -1){
				map.put("账单周期", cyclValue);
			}else{
				map.put("账单周期", cyclValue.substring(indexOf + 1, cyclValue.length()));
			}
			map.put("到期还款日", listString.get(listString.indexOf("到期还款日")+2));
			map.put("信用额度", listString.get(listString.indexOf("信用额度")+2));
			String value = listString.get(listString.indexOf("本期还款总额")+2);
			//本期还款总额=￥3,191.39;$93.93,
			String[] split = value.split(";");
			map.put("本期还款总额", split[0]);
			if (1 != split.length){
				map.put("本期还款总额$", split[1]);
			}
			map.put("预借现金额度", listString.get(listString.indexOf("预借现金额度")+2));
			String value2 = listString.get(listString.indexOf("本期最低还款额")+2);
			String[] split2 = value2.split(";");
			map.put("本期最低还款额", split2[0]);
			if (1 != split2.length){
				map.put("本期最低还款额$", split2[1]);
			}
			
			commonPart(listString, map);
			return map;
		}

		/**
		 * 获取用户的姓名及用户的性别
		 * @param listString
		 * @param map
		 */
		private void getUserNameAndSex(List<String> listString, Map<String, String> map) {
			int size = listString.size();
			for (int i = 0; i < size; i ++){
				String string = listString.get(i);
				if (string.contains("您好！")){
					int indexOf = string.indexOf("您好！");
					String substring = string.substring(string.contains("尊敬的") ? 3 : 0, indexOf-3);
					map.put("姓名", substring);
				}
				if (string.contains("女士")){
					map.put("性别", "女");
					break;
				}else if (string.contains("先生")){
					map.put("性别", "男");
					break;
				}
				map.put("性别", "");
				if (i > 10){
					break;
				}
			}
		}
		/**
		 * 获取账单的姓名跟性别
		 * @param listString
		 * @param map
		 */
		private void getNameAndSex(List<String> listString, Map<String, String> map) {
			String name = "";
			int size = listString.size();
			for (int i = 0; i < size; i ++){
				String string = listString.get(i);
				if (string.contains("尊敬的")){
					for (int j = i; j < size; j++){
						if (listString.get(j).contains("您好")){
							map.put("姓名", name);
							if (listString.get(j).contains("女士")){
								map.put("性别", "女");
								break;
							}else if (listString.get(j).contains("先生")){
								map.put("性别", "男");
								break;
							}
							break;
						}
						if (!listString.get(j).contains("尊敬的")){
							name += listString.get(j);
						}
					}
					break;
				}
				
				if (i > 10){
					break;
				}
			}
		}
		
		/**
		 * 获取账单的姓名跟性别
		 * @param listString
		 * @param map
		 */
		private void getNewUserNameAndSex(List<String> listString, Map<String, String> map) {
			String name = "";
			int size = listString.size();
			for (int i = 0; i < size; i ++){
				String string = listString.get(i);
				if (string.contains("尊敬的") && (string.contains("先生") || string.contains("女士")) && string.length() > 3){
					
					name = string.substring(3, string.indexOf("先生") == -1 ? string.indexOf("女士") : string.indexOf("先生"));
					map.put("姓名", name);
					if (string.contains("女士")){
						map.put("性别", "女");
						continue;
					}else if (string.contains("先生")){
						map.put("性别", "男");
						continue;
					}else{
						map.put("性别", "");
						continue;
					}
				}
				if (string.contains("可用额度")){
					int indexOf = string.indexOf("可用额度");
					int index = string.indexOf("取现额度");
					if (indexOf!=-1 && index != -1){
						String ke = string.substring(indexOf+5, index);
						map.put("可用额度", ke);
					}
					if (index != -1){
						String qu = string.substring(index+5, string.length());
						map.put("取现额度", qu);
					}
				}
			}
		}
		
		/**
		 * 招商银行信用卡电子账单
		 * 				-----招商银行信用卡电子账单
		 * 当期账单金额  、剩余应还金额  、 推荐分期金额  、分期后每期应还本金 、分期每期应付手续费 
		 * 放入map中
		 * @param listString
		 * @param map
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void fetchKeyMap3(List<String> listString, Map map) {
			int size = listString.size();
			for (int i = 0; i < size; i++){
				if (listString.get(i).startsWith("人民币账户")){
					break;
				}
				if (listString.get(i).startsWith("当期账单金额")){
					String substring = listString.get(i).substring(6, listString.get(i).length());
					map.put("当期账单金额", substring);
					continue;
				}
				if (listString.get(i).startsWith("剩余应还金额")){
					String substring = listString.get(i).substring(6, listString.get(i).length());
					map.put("剩余应还金额", substring);
					continue;
				}
			}
			map.put("推荐分期金额", listString.get(listString.indexOf("推荐分期金额")+1));
			map.put("分期后每期应还本金", listString.get(listString.indexOf("分期后每期应还本金")+1));
			map.put("分期每期应付手续费", listString.get(listString.indexOf("分期每期应付手续费")+1));
		}
		
		/**
		 * 招行商务卡账单
		 * 			------
		 * 账单日 、 到期还款日 、 信用额度 等等
		 * @param listString
		 * @param map
		 */
		private Map<String, String> fetchKeyMap1(List<String> listString, Map<String,String> map) {
			//抓取用户姓名与性别
			getUserNameAndSex(listString, map);
			// 抓取关键字的索引--》  旧版账单内的键值信息
			int size = listString.size();
			for (int i = 0 ; i < size; i++){
				if (listString.get(i).startsWith("账单日")){
					map.put("账单日", listString.get(i+1));
					continue;
				}
				if (listString.get(i).startsWith("到期还款日")){
					map.put("到期还款日", listString.get(i+1));
					continue;
				}
				if (listString.get(i).startsWith("信用额度")){
					map.put("信用额度", listString.get(i+1));
					continue;
				}
			}
			commonPart(listString, map);
			return map;
		}
		
		/**
		 * 招行个人信用卡新版账单
		 * 本期应还金额 、 最低还款额 、人民币交易、美金交易、当期账单金额、推荐分期金额、分期后当期应还、每期本金、每期手续费
		 * @param listString
		 * @param map
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map<String , String> fetchKeyMap2(List<String> listString, Map map) {
			
			//抓取用户姓名与性别
			getUserNameAndSex(listString, map);
			
			//抓取值后分类 
			int index = listString.indexOf("年")-1;
			int index1 = listString.indexOf("月信用卡个人卡账单已出，")-1;
			int index2 = listString.indexOf("月")-1;
			int index3 = listString.indexOf("日。")-1;
			if (-1 != index && -1 != index1){
				map.put("账期", listString.get(index)+"-"+listString.get(index1));
			}else{
				map.put("账期", null);
			}
			if (-1 != index && -1 != index2 && -1 != index3){
				map.put("最后还款日", listString.get(index)+"-"+listString.get(index2)+"-"+listString.get(index3));
			}else{
				map.put("最后还款日", null);
				
			}
			
			int size = listString.size();
			for (int i = 0; i < size; i++){
				if (listString.get(i).contains("本期应还金额")){
					map.put("本期应还金额", listString.get(i+1));
					map.put("本期应还金额$", listString.get(i+2));
				}
				if (listString.get(i).contains("最低还款额")){
					map.put("最低还款额", listString.get(i+1));
					map.put("最低还款额$", listString.get(i+2));
//					map.put("当期账单金额", listString.get(i+4));
//					map.put("推荐分期金额", listString.get(i+6));
//					map.put("分期后当期应还", listString.get(i+7));
//					map.put("每期本金", listString.get(i+9));
					return map;
				}
			}
			
			return map;
		}
		
		/**
		 * 抽取积分统计信息
		 * 积分“方程式”：
		 * 		可用积分余额 ＝ 上期可用积分余额 + 本期新增积分 + 本期调整积分 + 本期奖励积分 － 本期兑换积分总数
		 * @param listString
		 * @param map
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map<String, String> fetchCredits(List<String> listString, Map map) {
			//抓取值后分类 
			int indexOf = listString.indexOf("可用积分余额");
			if (indexOf != -1){
				map.put("可用积分余额", listString.get(indexOf+11));
			}
			int indexOf2 = listString.indexOf("上期可用积分余额");
			if (indexOf2 != -1){
				map.put("上期可用积分余额", listString.get(indexOf2+10));
			}
			int indexOf3 = listString.indexOf("本期新增积分");
			if (indexOf3 != -1){
				map.put("本期新增积分", listString.get(indexOf3+9));
			}
			int indexOf4 = listString.indexOf("本期调整积分");
			if (indexOf4 != -1){
				map.put("本期调整积分", listString.get(indexOf4+8));
			}
			int indexOf5 = listString.indexOf("本期奖励积分");
			if (indexOf5 != -1){
				map.put("本期奖励积分", listString.get(indexOf5+7));
			}
			int indexOf6 = listString.indexOf("本期兑换积分总数");
			if (indexOf6 != -1){
				map.put("本期兑换积分总数", listString.get(indexOf6+6));
			}
			return map;
		}
		/**
		 * 部分账户的提取“方程式”内容一致  
		 * 		本期应还金额 = 上期账单金额 - 上期还款金额 + 本期账单金额 - 本期调整金额 + 循环利息
		 * @param listString
		 * @param map
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map<String, String> commonPart(List<String> listString, Map map) {
			int size = listString.size();
			for (int i = 0; i < size; i++){
				if (listString.get(i).startsWith("人民币账户")){
					for (int j = i; j < size; j++){
						if (listString.get(j).startsWith("人民币本期应还金额")){
							map.put("人民币本期应还金额", listString.get(j+1));
							continue;
						}
						if (listString.get(j).startsWith("本期应还金额")){
							map.put("本期应还金额", listString.get(j+11));
							continue;
						}
						if (listString.get(j).startsWith("上期账单金额")){
							map.put("上期账单金额", listString.get(j+10));
							continue;
						}
						if (listString.get(j).startsWith("上期还款金额")){
							map.put("上期还款金额", listString.get(j+9));
							continue;
						}
						if (listString.get(j).startsWith("本期账单金额")){
							map.put("本期账单金额", listString.get(j+8));
							continue;
						}
						if (listString.get(j).startsWith("本期调整金额")){
							map.put("本期调整金额", listString.get(j+7));
							continue;
						}
						if (listString.get(j).startsWith("循环利息")){
							map.put("循环利息", listString.get(j+6));
							continue;
						}
						if (listString.get(j).startsWith("交易日")){
							break;
						}
					}
				}
				if (listString.get(i).startsWith("美元账户")){
					for (int j = i; j < size; j++){
						if (listString.get(j).startsWith("美元本期应还金额")){
							map.put("美元本期应还金额", listString.get(j+1));
							continue;
						}
						if (listString.get(j).startsWith("本期应还金额")){
							map.put("本期应还金额$", listString.get(j+11));
							continue;
						}
						if (listString.get(j).startsWith("上期账单金额")){
							map.put("上期账单金额$", listString.get(j+10));
							continue;
						}
						if (listString.get(j).startsWith("上期还款金额")){
							map.put("上期还款金额$", listString.get(j+9));
							continue;
						}
						if (listString.get(j).startsWith("本期账单金额")){
							map.put("本期账单金额$", listString.get(j+8));
							continue;
						}
						if (listString.get(j).startsWith("本期调整金额")){
							map.put("本期调整金额$", listString.get(j+7));
							continue;
						}
						if (listString.get(j).startsWith("循环利息")){
							map.put("循环利息$", listString.get(j+6));
							continue;
						}
						if (listString.get(j).startsWith("交易日") || listString.get(j).startsWith("[备注]") || listString.get(j).startsWith("★")){
							break;
						}
					}
				}
			}
			return map;
		}
	}
