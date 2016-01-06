package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
 * 平安银行账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PingAnBank implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(PingAnBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_PINGAN;
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
	 * 平安银行账单解析
	 * 
	 * @param lines			html过滤掉所有的标签后的String
	 * @param senderUrl		发件人地址
	 * @param oldHTML		原始的拼接地址信息
	 * @param newHtml		截取后拼接的地址信息
	 * @param id			更新时的id
	 * @param accountId		用户账号
	 * @param scVersion		服务端数据版本号
	 * @return	String		1 存在对应模板; 00 未找到相应模板;
	 */
	@Override
	public String bankTemplateParse(List<String> lines, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
		
		Map<String, String> billMapInfo = new HashMap<String, String>();
		List<String> handleList = new ArrayList<String>();
		// 判断账单类型
		 int flag = this.getFlag(lines);
		 log.info("calss:{} \tbankType:{}", new Object[]{PingAnBank.class, flag});
		// 改版前,单卡号或多卡号(无主副卡之分)处理
		if (flag == 1){
			billMapInfo.clear();
			handleList.clear();
			// 截取信息
			this.handleHtmlInfo(lines, handleList);
			log.info("--------改版前,单卡号或多卡号(无主副卡之分)处理---------");
//			if(CollectionUtils.isNotEmpty(handleList)){
//				int length = handleList.size();
//				for(int k = 0; k < length; k++){
//					log.info(k+"=="+handleList.get(k));
//				}
//			}
			// 汇总信息
			this.pingAnBankInfoMap(handleList, billMapInfo);
			// 消费明细
			List<List<String>> detailList = pingAnBankInfoList(handleList);
			// 处理多卡号问题, 将多卡号分组归类
			Map<String, List<List<String>>> oneOrMoreCardMap = null;
			if (CollectionUtils.isNotEmpty(detailList)) {
				oneOrMoreCardMap = new HashMap<String,List<List<String>>>(); // 初始化
				int length = detailList.size();
				for (int k = 0; k < length; k++) {
					List<String> rowInfo = detailList.get(k);
					if(CollectionUtils.isNotEmpty(rowInfo) && oneOrMoreCardMap.containsKey(rowInfo.get(2))){
						oneOrMoreCardMap.get(rowInfo.get(2)).add(rowInfo); // 将当前行追加到对应key值,value集合后
					} else {
						List<List<String>> cardOfDetailList = new ArrayList<List<String>>();
						cardOfDetailList.add(rowInfo);
						String key = StringUtils.isBlank(rowInfo.get(2)) ? "" : rowInfo.get(2);
						oneOrMoreCardMap.put(key, cardOfDetailList);
					}
				}
			}
			// 将多卡号处理成多卡持久化
			if (oneOrMoreCardMap.isEmpty()) {
				// map is null, do nothing
			} else {
				Set<String> cards = oneOrMoreCardMap.keySet();
				// 首先判断是否存在空卡号
				if(oneOrMoreCardMap.containsKey("")){ // 需合并
					// 将卡号为空,合并到消费明细多的项中 {key 明细条数;  value 卡号;}
					Map<Integer, String> cardMerger = new TreeMap<Integer, String>(new MapKeyComparator());
					for (String card_no : cards) {
						if (StringUtils.isNotBlank(card_no)){
							int size = oneOrMoreCardMap.get(card_no).size();
							cardMerger.put(Integer.valueOf(size), card_no);
						}
					}
					// 将卡号为空的项和合并到非空卡号的消费明细条数多的
					if (null != cardMerger && !cardMerger.isEmpty()) {
						// 获取明细最多的卡号
						Set<Integer> card_num = cardMerger.keySet();
						Object[] card_num_arr = card_num.toArray();
						String card_no = cardMerger.get(card_num_arr[card_num_arr.length-1]);
						// 追加到明细多的项中
						List<List<String>> nullCardInfo = oneOrMoreCardMap.get(""); // 空卡号的明细
						List<List<String>> dCardInfo = oneOrMoreCardMap.get(card_no);
						for (List<String> info : nullCardInfo) {
							dCardInfo.add(info);
						}
						// 合并后,将空卡号从oneOrMoreCardMap中移除
						oneOrMoreCardMap.remove("");
					}
				}
				if ( null != oneOrMoreCardMap && !oneOrMoreCardMap.isEmpty()) {
					// 多卡号处理
					Set<String> cardMerger = oneOrMoreCardMap.keySet();
					for (String card_no : cardMerger) {
						List<List<String>> oneCardInfo = oneOrMoreCardMap.get(card_no); // 一张卡的信息
						this.handleMoreCardBill(senderUrl, oldHTML, newHtml, id, accountId, scVersion, billMapInfo, oneCardInfo, card_no);
					}
				}
			}
			
			return "1";
		}
		
		// 新版账单,分主副卡标示
		if (flag == 2){
			log.info("--------新版账单,分主副卡标示处理---------");
			billMapInfo.clear();
			handleList.clear();
			// 截取信息
			this.handleNewBillInfo(lines, handleList);
//			if(CollectionUtils.isNotEmpty(handleList)){
//				int length = handleList.size();
//				for(int k = 0; k < length; k++){
//					log.info(k+"=="+handleList.get(k));
//				}
//			}
			// 1. 汇总信息收集
			this.newPingAnBankInfoMap(handleList, billMapInfo);
			// 2. 消费明细收集
			// 处理多卡号问题, 将多卡号分组归类
			Map<String, List<List<String>>> oneOrMoreCardMap = this.newPingAnBankInfoList(handleList);
			// 3. 多卡处理
			Set<String> cardMerger = oneOrMoreCardMap.keySet();
			for (String card_no : cardMerger) {
				List<List<String>> oneCardInfo = oneOrMoreCardMap.get(card_no); // 一张卡的信息
				this.handleMoreCardBill(senderUrl, oldHTML, newHtml, id, accountId, scVersion, billMapInfo, oneCardInfo, card_no);
			}
			
			return "1";
		}
		
		return "00";
	}
	
	// 存储各卡相关信息
	private void handleMoreCardBill(String senderUrl, String[] oldHTML, String[] newHtml, 
			Long[] id, Long accountId, Long scVersion, Map<String, String> billMapInfo,
			List<List<String>> detailList, String card_no) throws MailBillException{
		boolean update = false;
		// 如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		Long[] billId = null;
		
		// 1. 周期表信息
		BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
		billCycleInfoEntity.setAccountId(accountId);
		billCycleInfoEntity.setScVersion(scVersion);
		billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_PINGAN);
		billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
		billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
		billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_PINGAN);
		billCycleInfoEntity.setSenderUrl(senderUrl); // 获取发件人地址
		billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
		billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
		billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
		billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
		billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
		billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(billMapInfo.get("信用额度"))));
		billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.createDouble(Money.getNumber(billMapInfo.get("取现额度"))));
		billCycleInfoEntity.setBillDate(DateUtil.string2Day(billMapInfo.get("本期账单日")));
		billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(billMapInfo.get("本期账单日")),"yyyyMM"));
		billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(billMapInfo.get("本期还款日")));
		// 本期应还金额
		String nowPayment = billMapInfo.get("本期应还金额");
		if (StringUtils.isNotBlank(nowPayment)) {
			if (nowPayment.indexOf("{umP}") != -1){ // 新账单,有美元项. 人民币{umP}美元
				String[] payArr = nowPayment.split("\\{umP\\}");
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(payArr[0]))); // rmb
				billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(payArr[1]))); // usd
			} else { // 旧账单无美元项
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(billMapInfo.get("本期应还金额"))));
			}
		}
		// 本期最低还款额
		String minPayment = billMapInfo.get("本期最低还款额");
		if (StringUtils.isNotBlank(minPayment)){
			if (minPayment.indexOf("{umP}") != -1){  // 新账单,有美元项. 人民币{umP}美元
				String[] payArr = minPayment.split("\\{umP\\}");
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(payArr[0]))); // rmb
				billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.getNumber(payArr[1]))); // usd
			} else { // 旧账单无美元项
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(billMapInfo.get("本期最低还款额")))); // rmb
			}
		}
		
		billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(billMapInfo.get("本期余额")))); // rmb
		billCycleInfoEntity.setCardEndOfFour(card_no); // 卡号末四位
		billCycleInfoEntity.setUserName(billMapInfo.get("姓名"));
		if (!StringUtils.isBlank(billMapInfo.get("性别"))){
			billCycleInfoEntity.setUserGender(billMapInfo.get("性别"));
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
		
		// 2. 本期应还汇总信息
		BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
		if (update){
			billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
			balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
		}
		balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(billMapInfo.get("上期账单金额"))));
		balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(billMapInfo.get("上期还款金额"))));
		balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(billMapInfo.get("本期账单金额"))));
		balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.getNumber(billMapInfo.get("本期调整金额"))));
		balanceDetailEntity.setInterest(NumberTools.createDouble(Money.getNumber(billMapInfo.get("循环利息"))));
		// 本期应还金额
		String balanceNowPayment = billMapInfo.get("本期应还金额");
		if (StringUtils.isNotBlank(balanceNowPayment)) {
			if (balanceNowPayment.indexOf("{umP}") != -1){ // 新账单,有美元项. 人民币{umP}美元
				String[] payArr = balanceNowPayment.split("\\{umP\\}");
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(payArr[0]))); // rmb
			} else { // 旧账单无美元项
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(billMapInfo.get("本期应还金额"))));
			}
		}
		
		
		balanceDetailServiceImpl.save(balanceDetailEntity);
		
		// 3. 积分汇总信息
		IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
		if (update){
			billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
			integrationDetailEntity = detailServiceImpl.getId(billId[0]);
		}
		integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		integrationDetailEntity.setUsePoints(NumberTools.toInt(billMapInfo.get("本期余额")));
		integrationDetailEntity.setAddedPoints(NumberTools.toInt(billMapInfo.get("本期新增")));
		integrationDetailEntity.setRevisePoints(NumberTools.toInt(billMapInfo.get("本期调整")));
		integrationDetailEntity.setImpendingFailurePoints(billMapInfo.get("即将失效"));
		detailServiceImpl.save(integrationDetailEntity);
		
		if (update){
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
		}
		
		// 4. 消费明细信息
		if (CollectionUtils.isNotEmpty(detailList)){
			int length = detailList.size();
			for (int i = 0; i < length; i++){
				List<String> romInfo = detailList.get(i);
				if(CollectionUtils.isNotEmpty(romInfo)) {
					int rowLength = romInfo.size();
					for (int j = 0; j < rowLength; ){
						if(rowLength == 6){ // 新版账单, 6列
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(romInfo.get(j), "yyyy-MM-dd")); // 交易日期
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(romInfo.get(j+1), "yyyy-MM-dd")); // 记账日期
							bankMonthDetailEntity.setCardEndOfFour(romInfo.get(j+2)); // 卡号末四位
							bankMonthDetailEntity.setMerchandiseDetail(romInfo.get(j+3)); // 交易说明
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE); // 币种
							String strInput = romInfo.get(j+4); // 交易金额
							if (strInput.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
							bankMonthDetailEntity.setIsMaster(Integer.parseInt(romInfo.get(j+5))); //主/副卡标示
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += rowLength;
						} 
						
						if (rowLength == 8) { // 旧版账单, 8列
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(romInfo.get(j), "yyyy-MM-dd"));
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(romInfo.get(j+1), "yyyy-MM-dd"));
							bankMonthDetailEntity.setCardEndOfFour(romInfo.get(j+2));
							bankMonthDetailEntity.setMerchandiseDetail(romInfo.get(j+3));
							bankMonthDetailEntity.setCurrencyType(romInfo.get(j+4));
							if (StringUtils.isBlank(romInfo.get(j+5))){
								bankMonthDetailEntity.setMerchandiseArea(null);
							}else{
								bankMonthDetailEntity.setMerchandiseArea(romInfo.get(j+5));
							}
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
							if (StringUtils.isBlank(romInfo.get(j+6))){
								bankMonthDetailEntity.setOriginalTransAmount(0);
							}else{
								bankMonthDetailEntity.setOriginalTransAmount(NumberTools.createDouble(Money.getNumber(romInfo.get(j+6))));
							}
							String strInput = romInfo.get(j+7);
							if (strInput.contains("-")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
							bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
							j += rowLength;
						}
					}
				} else {
					// romInfo is null, do nothing
				}
			}
		} else {
			// detailList is null, do nothing
		}
	}
	
	// 根据账单特性,判断平安属于哪种类型
	private int getFlag(List<String> lines){
		int flag = 0;
		String replace = "";
//		log.info("pingan bank detail:{}", lines);
		if(CollectionUtils.isNotEmpty(lines)){
			int length = lines.size();
			for(int k = 0; k < length; k++){
//				log.info(k+"=="+lines.get(k));
				replace = lines.get(k).replace("\\s", "").replace("&nbsp;", "");
				// 普通类型的账单(旧版的,单卡号\多卡号(无主副卡标示))
				if(replace.startsWith("您的信用卡账户信息")){
					flag = 1;
					return flag;
				}
				// 新版账单,分主副卡标示
				if(replace.startsWith("账户信息")){
					flag = 2;
					return flag;
				}
			}
		}
		
		return flag;
	}
	
	/**
	 * 旧版账单 -- 截取需要的字符串
	 * @param lines			含不需要数据的字符
	 * @param handleList	截取有用的
	 */
	private void handleHtmlInfo(List<String> lines, List<String> handleList) {
		String replace;
		if(CollectionUtils.isNotEmpty(lines)){
			int size = lines.size();
			for(int i = 1; i < size; i++){
				if (lines.get(i).contains("尊敬的")){	
					replace = lines.get(i).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					handleList.add(replace);
				}
				if (lines.get(i).startsWith("信用额度")){	
					for (int jj = i; jj < size; jj++){
						replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
						//详细内容的提取
						if(replace.startsWith("最新优惠")){
							break;
						}
						handleList.add(replace);
					}
				}
				if (lines.get(i).startsWith("本期应还款金额（人民币账户）")){	
					for (int jj = i; jj < size; jj++){
						replace = lines.get(jj).replace("\\s", "").replace("&nbsp;", "");
						//详细内容的提取
						handleList.add(replace);
						if(replace.startsWith("用卡小贴士")){
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 新版账单 -- 截取需要的字符串
	 * 
	 * @param lines
	 *            含不需要数据的字符
	 * @param handleList
	 *            截取有用的
	 */
	private void handleNewBillInfo(List<String> lines, List<String> handleList) {

		String replace = "";
		boolean flag = true;

		if (CollectionUtils.isNotEmpty(lines)) {
			int size = lines.size();
			for (int i = 1; i < size; i++) {
				if (flag && !lines.get(i).contains("尊敬的")) {
					continue;
				} else {
					flag = false;
					replace = lines.get(i).replace("\\s", "").replace("&nbsp;", "");
					if (!replace.contains("手机客户端还款")
						&& !replace.contains("平安银行在此温馨提醒您")
						&& !replace.contains("平安信用卡官网")) {
						handleList.add(replace);
					} else {
						if(replace.contains("平安信用卡官网")){
							break;
						}else {
							continue;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 新版多卡号 -- 存储平安银行解析后的一次性数据
	 * 
	 * @param handleList 	处理并过滤后的HTML内容
	 * @param billMapInfo	存放需要的一次性数据
	 * @return 
	 */
	private void newPingAnBankInfoMap(List<String> handleList, Map<String, String> billMapInfo){
		// 1.用户姓名跟性别的提取
		GetUserInfoUtil.getUserInfoFor(handleList, billMapInfo, 3, 3);
		
		// 2.用户卡账户信息开始
		billMapInfo.put("信用额度", Money.removePrefix(handleList.get(handleList.indexOf("信用额度")+1)));
		billMapInfo.put("取现额度", Money.removePrefix(handleList.get(handleList.indexOf("取现额度")+1)));
		billMapInfo.put("本期账单日", handleList.get(handleList.indexOf("本期账单日")+1));
		// 新版中追加了美元部分, value: 人民币{umP}美元 
		String rmb = Money.removePrefix(handleList.get(handleList.indexOf("本期应还金额")+2));
		String usd = Money.removePrefix(handleList.get(handleList.indexOf("本期应还金额")+3));
		billMapInfo.put("本期应还金额", rmb+"{umP}"+usd);
		billMapInfo.put("本期还款日", handleList.get(handleList.indexOf("本期还款日")+1));
		// 新版中追加了美元部分, value: 人民币{umP}美元
		String payRmb = Money.removePrefix(handleList.get(handleList.indexOf("本期最低应还金额")+3));
		String payUsd = Money.removePrefix(handleList.get(handleList.indexOf("本期最低应还金额")+4));
		billMapInfo.put("本期最低还款额", payRmb+"{umP}"+payUsd);
		
		// 3.万里通积分开始
		billMapInfo.put("本期余额", handleList.get(handleList.indexOf("本期余额")+5));
		billMapInfo.put("本期新增", handleList.get(handleList.indexOf("本期新增")+5));
		billMapInfo.put("本期调整", handleList.get(handleList.indexOf("本期调整")+5));
		billMapInfo.put("即将失效", handleList.get(handleList.indexOf("即将失效")+5));

		// 4.本期应还款金额（人民币账户）开始
		billMapInfo.put("上期账单金额", Money.removePrefix(handleList.get(handleList.indexOf("上期账单金额PreStatement")+10)));
		billMapInfo.put("上期还款金额", Money.removePrefix(handleList.get(handleList.indexOf("上期还款金额PrePayment")+9)));
		billMapInfo.put("本期账单金额", Money.removePrefix(handleList.get(handleList.indexOf("本期账单金额CurStatement")+8)));
		billMapInfo.put("本期调整金额", Money.removePrefix(handleList.get(handleList.indexOf("本期调整金额CurAdjustment")+7)));
		billMapInfo.put("循环利息", Money.removePrefix(handleList.get(handleList.indexOf("循环利息CycleInterest")+6)));
	}
	
	/**
	 * 旧版单卡及多卡 -- 存储平安银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param handleList 	处理并过滤后的HTML内容
	 * @param billMapInfo	存放需要的一次性数据
	 * @return 
	 */
	private void pingAnBankInfoMap(List<String> handleList, Map<String, String> billMapInfo){
		
		// 用户姓名跟性别的提取
		GetUserInfoUtil.getUserInfoFor(handleList, billMapInfo, 3, 3);
		
		// 用户卡账户信息开始
		billMapInfo.put("信用额度", handleList.get(handleList.indexOf("信用额度")+1));
		billMapInfo.put("取现额度", handleList.get(handleList.indexOf("取现额度")+1));
		billMapInfo.put("本期账单日", handleList.get(handleList.indexOf("本期账单日")+1));
		billMapInfo.put("本期应还金额", handleList.get(handleList.indexOf("本期应还金额")+1));
		billMapInfo.put("本期还款日", handleList.get(handleList.indexOf("本期还款日")+1));
		billMapInfo.put("本期最低还款额", handleList.get(handleList.indexOf("本期最低还款额")+1));
		
		//万里通积分开始
		billMapInfo.put("本期余额", handleList.get(handleList.indexOf("本期余额")+1));
		billMapInfo.put("本期新增", handleList.get(handleList.indexOf("本期新增")+1));
		billMapInfo.put("本期调整", handleList.get(handleList.indexOf("本期调整")+1));
		billMapInfo.put("即将失效", handleList.get(handleList.indexOf("即将失效")+1));


		//本期应还款金额（人民币账户）开始
		String temp;
//		temp = list.get(list.indexOf("本期应还款金额（人民币账户）")+1);	//与上面的”本期应还金额“的值相同
//		temp = temp.substring(temp.indexOf("RMB"));
//		map.put("本期应还金额", temp);
		int index = handleList.indexOf("本期应还款金额（人民币账户）")+2;
		if (index < handleList.size()){
			temp = handleList.get(index);
			temp = temp.substring(temp.indexOf("RMB"));
			billMapInfo.put("上期账单金额", temp);
		}
		
		temp = handleList.get(handleList.indexOf("本期应还款金额（人民币账户）")+3);
		temp = temp.substring(temp.indexOf("RMB"));
		billMapInfo.put("上期还款金额", temp);
		
		temp = handleList.get(handleList.indexOf("本期应还款金额（人民币账户）")+4);
		temp = temp.substring(temp.indexOf("RMB"));
		billMapInfo.put("本期账单金额", temp);
		
		temp = handleList.get(handleList.indexOf("本期应还款金额（人民币账户）")+5);
		temp = temp.substring(temp.indexOf("RMB"));
		billMapInfo.put("本期调整金额", temp);
		
		temp = handleList.get(handleList.indexOf("本期应还款金额（人民币账户）")+6);
		temp = temp.substring(temp.indexOf("RMB"));
		billMapInfo.put("循环利息", temp);
		
		//交易明细（人民币账户）为循环数据 通过PingAnBankInfoList方法存储
		//return billMapInfo;
	}
	
	/**
	 * 新版单卡及多卡 -- 存储平安银行消费明细
	 * 1.交易日期 2.记账日期 3.交易说明 4.人民币金额
	 * 
	 * @param Map<String, List<List<String>>> 按卡号分组归类, key:卡号 ;value:消费明细二维数组
	 */
	private Map<String, List<List<String>>> newPingAnBankInfoList(List<String> handleList){
		Map<String, List<List<String>>> oneOrMoreCardMap = null;
		String mainCardNo = ""; // 主卡卡号
		String supCardNo = ""; // 附卡卡号
		if (CollectionUtils.isNotEmpty(handleList)) {
			int length = handleList.size();
			oneOrMoreCardMap = new HashMap<String, List<List<String>>>();
			// 主卡 
			if(handleList.indexOf("主卡MainCard") != -1){
				int mainIndex = handleList.indexOf("主卡MainCard")+1;
				String mainCardNoStr = handleList.get(mainIndex); // 主卡卡号项
				mainCardNo = mainCardNoStr.substring("平安银行平安保险银联金卡********".length());
				
				List<List<String>> mainCardInfo = new ArrayList<List<String>>();
				for ( int k = mainIndex+1; k < length; ) {
					String temp = handleList.get(k);
					if(temp.equals("附卡SupCard") || temp.equals("其他Other")){ // 到附卡\其他Other部分就跳出
						break;
					}
					
					List<String> row = new ArrayList<String>();
					row.add(handleList.get(k));
					row.add(handleList.get(k+1));
					row.add(mainCardNo); // 卡号末四位
					row.add(handleList.get(k+2));
					row.add(Money.removePrefix(handleList.get(k+3)));
					row.add(String.valueOf(MailBillTypeConstants.BANK_MAIN_CARD)); // 主卡
					 // 存储一行消费明细
					mainCardInfo.add(row);
					k += 4;
				}
				// 主卡相关信息收集
				oneOrMoreCardMap.put(mainCardNo, mainCardInfo); 
			}
			// 附卡
			if(handleList.indexOf("附卡SupCard") != -1){
				int supIndex = handleList.indexOf("附卡SupCard")+1;
				String supCardNoStr = handleList.get(supIndex); // 附卡卡号项
				supCardNo = supCardNoStr.substring("平安银行平安保险银联金卡********".length());
				
				List<List<String>> supCardInfo = new ArrayList<List<String>>();
				for ( int k = supIndex+2; k < length; ) {
					String temp = handleList.get(k);
					if(temp.equals("其他Other")){ // 到其他Other部分就跳出
						break;
					}
					
					List<String> row = new ArrayList<String>();
					row.add(handleList.get(k));
					row.add(handleList.get(k+1));
					row.add(supCardNo); // 卡号末四位
					row.add(handleList.get(k+2));
					row.add(Money.removePrefix(handleList.get(k+3)));
					row.add(String.valueOf(MailBillTypeConstants.BANK_VICE_CARD)); // 附卡
					 // 存储一行消费明细
					supCardInfo.add(row);
					k += 4;
				}
				// 附卡相关信息收集
				oneOrMoreCardMap.put(supCardNo, supCardInfo); 
			}
			
			// 其他明细
			if (handleList.indexOf("其他Other") != -1) {
				int otherIndex = handleList.indexOf("其他Other")+1;
				handleList.get(otherIndex); // 其他项(合并到主卡项中)
				List<List<String>> otherCardInfo = new ArrayList<List<String>>();
				for ( int k = otherIndex; k < length; ) {
					String temp = handleList.get(k);
					if(StringUtils.isBlank(temp)){ // 遇到空部分就跳出
						break;
					}
					
					List<String> row = new ArrayList<String>();
					row.add(handleList.get(k));
					row.add(handleList.get(k+1));
					row.add(""); // 卡号末四位
					row.add(handleList.get(k+2));
					row.add(Money.removePrefix(handleList.get(k+3)));
					row.add(String.valueOf(MailBillTypeConstants.BANK_MAIN_CARD)); // 主卡
					 // 存储一行消费明细
					otherCardInfo.add(row);
					k += 4;
				}
				
				// 主卡相关信息
				List<List<String>> mainCardInfo = oneOrMoreCardMap.get(mainCardNo); 
				// 合并附属卡信息到主卡明细中
				if(CollectionUtils.isNotEmpty(otherCardInfo)){
					for(List<String> item : otherCardInfo){
						mainCardInfo.add(item);
					}
				}
			}
		}
		
		return oneOrMoreCardMap;
	}
	
	/**
	 * 旧版单卡及多卡 -- 存储平安银行消费明细（循环数据）
	 * 1.交易日期 2.记账日期 3.卡号末四位 4.交易摘要 5.交易地点 6.交易币种 7.交易地金额 8.人民币金额
	 * 
	 * @param handleList 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> pingAnBankInfoList(List<String> handleList){
		List<List<String>> detailList = new ArrayList<List<String>>();
		int start = handleList.indexOf("人民币金额")+1;	// 明细信息开始位置
		int end = handleList.indexOf("用卡小贴士")-1;	// 明细信息结束位置
		final int count_field = 8;	//明细字段数
		int listSize = (end+1 - start )/count_field; // 记录明细信息的条数
		for(int i=0;i<listSize;i++){
			List<String> listtemp = new ArrayList<String>();
			for(int j=0;j<count_field;j++){
				listtemp.add(handleList.get(start));
				start++;
			}
			detailList.add(listtemp);
		}
		return detailList;
	}
	
	// 比较器类  
	public class MapKeyComparator implements Comparator<Integer> {
		public int compare(Integer c1, Integer c2) {
			int flag = 0;
			if (c1 == c2) {
				flag = 0;
			}
			if (c1 > c2) {
				flag = 1;
			}
			if (c1 < c2) {
				flag = -1;
			}

			return flag;
		}
	}
}
