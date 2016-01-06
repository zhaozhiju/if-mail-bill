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
 * 农业银行邮件账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AgriculturalBank  implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(AgriculturalBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_AGRICULTURAL;
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
	public String bankTemplateParse(List<String> lines, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {

		boolean update = false;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		Map<String, String> map = new HashMap<String, String>();
		List<String> listString = new ArrayList<String>();
		Long[] billId = null;
		int flag = 0;
		flag = getString(lines, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{AgriculturalBank.class, flag});
		if (flag == 1){
//			log.info("农行 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.汇总信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 2.交易明细
			List<List<String>> detailInfoList = datailList(listString, 7);
			// 3.账单周期表
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_AGRICULTURAL);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_AGRICULTURAL);
			billCycleInfoEntity.setSenderUrl(senderUrl); // 获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			String emailInfo = ReadProperty.getEmailInfo(oldHTML[0], 2);
			if (StringUtils.isNotBlank(emailInfo)){
				Date stringToDate = DateUtil.stringToDate(emailInfo, "yyyy-MM-dd HH:mm:ss");
				billCycleInfoEntity.setSentData(stringToDate);
			}
			billCycleInfoEntity.setReceiveAddUrl(ReadProperty.getEmailInfo(oldHTML[0], 1));
			billCycleInfoEntity.setSubject(ReadProperty.getEmailInfo(oldHTML[0], 0));
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String dateStr = fetchKeyMap.get("账单日");
			if (dateStr != null){
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(dateStr));
				billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(dateStr,"yyyyMMdd"), "yyyyMM"));
			}
			String paymentDueDate = fetchKeyMap.get("到期还款日");
			if (paymentDueDate != null){
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(paymentDueDate, "yyyyMMdd"));
			}
			String minRmbPayment = fetchKeyMap.get("最低还款额");
			if (minRmbPayment != null){
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(minRmbPayment));
			}
			String minUsaPayment = fetchKeyMap.get("最低还款额$");
			if (StringUtils.isNotBlank(minUsaPayment)){
				billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(minUsaPayment));
			}
			String rmbIntegration = fetchKeyMap.get("本期积分余额");
			if (StringUtils.isNotBlank(rmbIntegration)){
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(rmbIntegration)));
			}
			String rmbBalance = fetchKeyMap.get("本期账户全部余额");
			if (StringUtils.isNotBlank(rmbBalance)){
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(rmbBalance)));
			}
			String usaBalance = fetchKeyMap.get("本期账户全部余额$");
			if (StringUtils.isNotBlank(usaBalance)){
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(usaBalance)));
			}
			String rmbCredit = fetchKeyMap.get("信用额度");
			if (StringUtils.isNotBlank(rmbCredit)){
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(rmbCredit)));
			}
			String usaCredit = fetchKeyMap.get("信用额度$");
			if (StringUtils.isNotBlank(usaCredit)){
				billCycleInfoEntity.setUsaCreditLimit(NumberTools.createDouble(Money.getNumber(usaCredit)));
			}
			String newRmbBalance = fetchKeyMap.get("本期全部应还款额");
			if (StringUtils.isNotBlank(newRmbBalance)){
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(newRmbBalance)));
			}
			String newUsdBalance = fetchKeyMap.get("本期全部应还款额$");
			if (StringUtils.isNotBlank(newUsdBalance)){
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(newUsdBalance)));
			}
			
			// 卡号末四位
			String mainCradNo = fetchKeyMap.get("主卡卡号");
			if(StringUtils.isNotBlank(mainCradNo)){
				billCycleInfoEntity.setCardEndOfFour(mainCradNo.substring(mainCradNo.length()-4, mainCradNo.length()));
			} else {
				String card = detailInfoList.get(0).get(2);
				if (StringUtils.isNotBlank(card)){
					billCycleInfoEntity.setCardEndOfFour(card);
				}
			}
			String userName = fetchKeyMap.get("姓名");
			if (StringUtils.isNotBlank(userName)){
				billCycleInfoEntity.setUserName(userName);
			}
			if (StringUtils.isNotBlank(fetchKeyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(fetchKeyMap.get("性别"));
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
			
			// 4.1.本期应还款详情 - rmb
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			String balance = fetchKeyMap.get("上期余额");
			if (StringUtils.isNotBlank(balance)){
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(balance)));
			}
			String payment = fetchKeyMap.get("本期已还款额");
			if (StringUtils.isNotBlank(payment)){
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(payment)));
			}
			String newCharges = fetchKeyMap.get("本期新增应还款额");
			if (StringUtils.isNotBlank(newCharges)){
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(newCharges)));
			}
			String nowRmbBalance = fetchKeyMap.get("本期账户全部余额");
			if (StringUtils.isNotBlank(nowRmbBalance)){
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(nowRmbBalance)));
			}
			balanceDetailServiceImpl.save(balanceDetailEntity);
			// 4.2.本期应还款详情 - usd
			BalanceDetailEntity balanceDetailUsaEntity = new BalanceDetailEntity();
			if (update){
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[1]);
			}
			balanceDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
			balanceDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			String usaBalance1 = fetchKeyMap.get("上期余额$");
			if (StringUtils.isNotBlank(usaBalance1)){
				balanceDetailUsaEntity.setBalance(NumberTools.createDouble(Money.getNumber(usaBalance1)));
			}
			String usaPayment1 = fetchKeyMap.get("本期已还款额$");
			if (StringUtils.isNotBlank(usaPayment1)){
				balanceDetailUsaEntity.setPayment(NumberTools.createDouble(Money.getNumber(usaPayment1)));
			}
			String usaNewCharge = fetchKeyMap.get("本期新增应还款额$");
			if (StringUtils.isNotBlank(usaNewCharge)){
				balanceDetailUsaEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(usaNewCharge)));
			}
			String usaNewBalance = fetchKeyMap.get("本期账户全部余额$");
			if (StringUtils.isNotBlank(usaNewBalance)){
				balanceDetailUsaEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(usaNewBalance)));
			}
			balanceDetailServiceImpl.save(balanceDetailUsaEntity);
			
			// 5.积分明细开始
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			if (update){
				billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = detailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			String strInput = fetchKeyMap.get("本期兑换（调整）积分");
			if (StringUtils.isNotBlank(strInput)){
				integrationDetailEntity.setExchangePoints(NumberTools.getNumber(Money.getNumber(strInput)));
			}
			String strInput2 = fetchKeyMap.get("本期新增积分");
			if (StringUtils.isNotBlank(strInput2)){
				integrationDetailEntity.setAddedPoints(NumberTools.getNumber(Money.getNumber(strInput2)));
			}
			if (StringUtils.isNotBlank(rmbIntegration)){
				integrationDetailEntity.setUsePoints(NumberTools.getNumber(Money.getNumber(rmbIntegration)));
			}
			detailServiceImpl.save(integrationDetailEntity);
			
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			
			// 6.交易明细
			if(CollectionUtils.isNotEmpty(detailInfoList)) {
				int size = detailInfoList.size();
				for (int i = 0; i < size; i++){
					List<String> rowInfo = detailInfoList.get(i);
					int rowLength = rowInfo.size();
					for (int j = 0; j < rowLength; ){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						if(StringUtils.isNotBlank(rowInfo.get(j))){ // 交易日期
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyyMMdd"));
						}
						if(StringUtils.isNotBlank(rowInfo.get(j+1))){ // 记账日期
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyyMMdd"));
						}
						if(StringUtils.isNotBlank(rowInfo.get(j+2))){ // 卡号末四位
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+2));
						}
						if(StringUtils.isNotBlank(rowInfo.get(j+3))){
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3)); // 摘要信息
						}
						
						bankMonthDetailEntity.setMerchandiseArea(rowInfo.get(j+4)); // 交易地点
						String amount = rowInfo.get(j+6);
						if(StringUtils.isNotBlank(amount)){
							if (!amount.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME); //收入
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(amount))); // 交易金额
							String currency = amount.substring(amount.length()-3, amount.length());
							bankMonthDetailEntity.setCurrencyType(currency.toUpperCase()); // 币种
						}
						
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += rowLength;
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
		for(int i = 1; i < size; i++){
			if (lines.get(i).contains("尊敬的")){	
				replace = lines.get(i).replace("\\s", "").replace("&nbsp;", "");
				listString.add(replace);
				continue;
			}
			if (lines.get(i).contains("白金信用卡电子对账单")){	
				for (int jj = i; jj < size; jj++){
					replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					if(replace.contains("温馨提示")){
						flag = 1;
						break;
					}
					if (StringUtils.isNotBlank(replace)){
						listString.add(replace);
					}
				}
			}
		}
		return flag;
	}
	
	/**
	 * 存储农业银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list解析后的数据
	 * @param map
	 */
	private Map<String, String> fetchKeyMap(List<String> list, Map<String, String> map){
		String[] split ;
		int size = list.size();
		for (int i = 0 ; i < size; i++){
			String string = list.get(i);
			if (string.contains("尊敬的")){
				if (string.length() >= 6){
					String substring = string.substring(3, string.length()-6);
					map.put("姓名", substring);
					map.put("性别", "");
				}
				continue;
			}
			if (string.contains("主卡卡号")){
				if(string.indexOf(":") != -1){
					split = string.split(":");
					map.put("主卡卡号", split[1]);
					continue;
				}
			}
			if (string.contains("账单日")){
				split = string.split(":");
				map.put("账单日", split[1]);
				continue;
			}
			if (string.contains("到期还款日")){
				split = string.split(":");
				map.put("到期还款日", split[1]);
				continue;
			}
			if (string.contains("信用额度")){
				if (i+4 < size)
					map.put("信用额度", list.get(i+4));
				if (i+8 < size)
					map.put("信用额度$", list.get(i+8));
				continue;
			}
			if (string.contains("本期全部应还款额")){
				if (i+4 < size)
					map.put("本期全部应还款额", list.get(i+4));
				if (i+8 < size)
					map.put("本期全部应还款额$", list.get(i+8));
				continue;
			}
			if (string.contains("最低还款额")){
				if (i+4 < size)
					map.put("最低还款额", list.get(i+4));
				if (i+8 < size)
					map.put("最低还款额$", list.get(i+8));
				continue;
			}
			if (string.contains("上期余额")){
				if (i+6 < size)
					map.put("上期余额", list.get(i+6));
				if (i+12 < size)
					map.put("上期余额$", list.get(i+12));
				continue;
			}
			if (string.contains("本期新增应还款额")){
				if (i+6 < size)
					map.put("本期新增应还款额", list.get(i+6));
				if (i+12 < size)
					map.put("本期新增应还款额$", list.get(i+12));
				continue;
			}
			if (string.contains("本期已还款额")){
				if (i+6 < size)
					map.put("本期已还款额", list.get(i+6));
				if (i+12 < size)
					map.put("本期已还款额$", list.get(i+12));
				continue;
			}
			if (string.contains("本期账户全部余额")){
				if (i+6 < size)
					map.put("本期账户全部余额", list.get(i+6));
				if (i+12 < size)
					map.put("本期账户全部余额$", list.get(i+12));
				continue;
			}
			if (string.contains("本期新增积分")){
				if (i+3 < size)
					map.put("本期新增积分", list.get(i+3));
				continue;
			}
			if (string.contains("本期兑换（调整）积分")){
				if (i+3 < size)
					map.put("本期兑换（调整）积分", list.get(i+3));
				continue;
			}
			if (string.contains("本期积分余额")){
				if (i+3 < size)
					map.put("本期积分余额", list.get(i+3));
				continue;
			}
		}
		return map;
	}
	
	/**
	 * 存储农业银行消费明细（循环数据）
	 * 1.交易日 2.记账日 3.卡号后四位 4.交易摘要 5.交易地点6.交易金额/币种 7.入账金额/币种（支出为-）
	 * 
	 * @param list 		处理并过滤后的数据
	 * @param colums 	列数
	 */
	private List<List<String>> datailList(List<String> list, int colums){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		boolean ifEnd = false;
		if (CollectionUtils.isNotEmpty(list)) {
			int size2 = list.size();
			for (int i = 0; i < size2; i++){
				if (list.get(i).contains("交易日")){
					i += 7;
					for (int j = i ; j < size2; j++){
						// 交易详细存放如二维数组内
						List<String> rowInfo = new ArrayList<String>(); // 临时存储数据
						for(int a = 0; a < colums; a++){
							int cou = j + a;
							rowInfo.add(list.get(cou)); // 以cou为一组放入rowInfo内
						}
						j += colums - 1;
						listDouble.add(rowInfo); // 将rowInfo放入list内形成交易详细的二维数组
						if (list.get(j+1).contains("本期新增积分")){
							ifEnd = true;
							break;
						}
					}
				}
				if (ifEnd == true) { // 结束无用的循环
					break;
				}
			}
		}
		
		return listDouble;
	}
}
