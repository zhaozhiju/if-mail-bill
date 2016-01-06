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
 * 对华夏银行的邮件账单的解析及取值
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class HuaxiaBank implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HuaxiaBank.class);
	
	@Override
	public int getBankType(){
		return MailBillTypeConstants.BANK_TYPE_HUAXIA;
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
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		List<String> listString = new ArrayList<String>();	//存储所有数据
		Map<String, String> map = new HashMap<String, String>();//抓取值
		int flag = 0;
		Long[] billId = null;
		listString.clear();
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{HuaxiaBank.class, flag});
		if (flag == 1){
//			log.info("银联 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.账务汇总信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 2.应还款汇总信息
			Map<String, String> fetchKeyMapRBMAndUSD = fetchKeyMapRBMAndUSD(listString, map);
			// 3.积分统计信息
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			// 4.交易明细列表
			List<List<String>> detailList = tradingDetailList(listString, 5);
			// 5.账单周期表
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_HUAXIA);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_HUAXIA);
			billCycleInfoEntity.setSenderUrl(senderUrl);//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			String formatDate = DateUtil.getFormatDate(DateUtil.string2DateMonth(fetchKeyMap.get("华夏信用卡对账单(年月)")), "yyyyMM");
			int string2Day = DateUtil.string2Day(fetchKeyMap.get("账单日"));
			billCycleInfoEntity.setAccountOfDate(formatDate);
			billCycleInfoEntity.setBillDate(string2Day);
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMap.get("本期到期还款日")));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("信用额度"))));
			billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("预借现金额度"))));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期应还金额"))));
			billCycleInfoEntity.setNewUsaBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("本期应还金额$"))));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("最低还款额")))); // rmb
			billCycleInfoEntity.setMinUsaPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("最低还款额$")))); // usa
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(fetchCredits.get("本期账单积分余额")))); // 可用积分余额
			
			if(CollectionUtils.isNotEmpty(detailList)) { // 可能存在首行无卡号,依次往下找
				int length = detailList.size();
				for(int k = 1; k < length; k++){ // 第二行开始
					if(StringUtils.isNotBlank(detailList.get(k).get(4))){
						billCycleInfoEntity.setCardEndOfFour(detailList.get(k).get(4)); // 卡号末四位
						break;
					}
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
			if (update){
				billCycleInfoEntity.setId(id[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			// 6.1.应还款汇总信息 - rmb
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("上期对账单金额"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期新增账款"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期已还款金额"))));
			balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期调整金额"))));
			balanceDetailEntity.setInterest(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("循环利息"))));
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期账单余额"))));
			balanceDetailServiceImpl.save(balanceDetailEntity);
			// 6.2.美元的账单金额汇总  - usd
			BalanceDetailEntity balanceDetailEntityUsa = new BalanceDetailEntity();
			if (update){
				balanceDetailEntityUsa = balanceDetailServiceImpl.getId(billId[1]);
			}
			balanceDetailEntityUsa.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
			balanceDetailEntityUsa.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntityUsa.setBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("上期对账单金额$"))));
			balanceDetailEntityUsa.setNewCharges(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期新增账款$"))));
			balanceDetailEntityUsa.setPayment(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期已还款金额$"))));
			balanceDetailEntityUsa.setAdjustment(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期调整金额$"))));
			balanceDetailEntityUsa.setInterest(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("循环利息$"))));
			balanceDetailEntityUsa.setNewBalance(NumberTools.createDouble(Money.getNumber(fetchKeyMapRBMAndUSD.get("本期账单余额$"))));
			balanceDetailServiceImpl.save(balanceDetailEntityUsa);
			
			billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
			
			// 7.积分详情
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				integrationDetailEntity = detailServiceImpl.getId(billId[1]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("本期账单积分余额"))));
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("上期积分余额"))));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("本期新增积分"))));
			integrationDetailEntity.setAwardPoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("本期奖励积分"))));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("本期调整积分"))));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.getNumber(fetchCredits.get("本期兑换积分"))));
			detailServiceImpl.save(integrationDetailEntity);
			
			int f = 0;
			//详细交易  TODO根据卡号不同交易代做
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			int size = detailList.size();
			for (int i = 0; i < size; i++){
				if (i == 0){
					continue;
				}
				List<String> rowInfo = detailList.get(i);
				if (rowInfo.get(0).contains("交易")){
					f = 1;
					continue;
				}
				if (f == 0){
					for (int j = 0; j < rowInfo.size(); ){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j)));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1)));
						bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+2));
						String strInput = rowInfo.get(j+3);
						if (strInput.contains("-")){
							bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						}
						bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
						bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+4));
						bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += 5;
					}
				}
				if (f == 1 && 0 != rowInfo.size()){
					for (int k = 0; k < rowInfo.size();){
						BillBankMonthDetailEntity bankMonthDetailUsaEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailUsaEntity = bankMonthDetailServiceImpl.getId(billId[i+k]);
							bankMonthDetailUsaEntity.setId(bankMonthDetailUsaEntity.getId());
						}
						bankMonthDetailUsaEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailUsaEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(k)));
						bankMonthDetailUsaEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(k+1)));
						bankMonthDetailUsaEntity.setMerchandiseDetail(rowInfo.get(k+2));
						String strInput = rowInfo.get(k+3);
						if (strInput.contains("-")){
							bankMonthDetailUsaEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
						}
						bankMonthDetailUsaEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
						bankMonthDetailUsaEntity.setCardEndOfFour(rowInfo.get(k+4));
						bankMonthDetailUsaEntity.setCurrencyType(MailBillTypeConstants.USD_CURRENCY_TYPE);
						bankMonthDetailServiceImpl.save(bankMonthDetailUsaEntity);
						k += 5;
					}
				}
			}
			return "1";
		}
		return "00";
	}

	/**
	 * 获取字符串List
	 * @param parse	原字符串
	 * @param listString	截取后的字符串
	 */
	private int getStringList(List<String> parse, List<String> listString, int flag) {
		int size = parse.size();
		for (int i = 0; i < size; i++){
			if (parse.get(i).startsWith("华夏信用卡对账单")){
				for (int j = i; j < size; j++){
					//详细内容的提取
					listString.add(parse.get(j)); 		//将需要的数据添加到listString里
					if (parse.get(j).startsWith("说明Instruction")){
						flag = 1;
						break;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 对其中信息的提取
	 * 		a)	账单日 、本期到期还款日、信用额度 （元）、预借现金额度 （元）、本期应还金额（包含RBM与USD）、最低还款额
	 * 
	 * @param listString	--截取需要的数据的内容
	 * @param map	--抓取值后存放在map内
	 */
	private Map<String, String> fetchKeyMap(List<String> listString, Map<String,String> map) {
		int size = listString.size();
		for (int i = 0; i < size; i++){
			String string = listString.get(i);
			GetUserInfoUtil.getUserInfo(map, string, 3, 3);
			if (string.startsWith("华夏信用卡对账单")){
				String substring = string.substring(8);
				map.put("华夏信用卡对账单(年月)", substring);
				continue;
			}
			if (string.startsWith("账单日")){
				map.put("账单日", listString.get(i+1));
				continue;
			}
			if (string.startsWith("本期到期还款日")){
				map.put("本期到期还款日", listString.get(i+1));
				continue;
			}
			if (string.startsWith("信用额度")){
				map.put("信用额度", listString.get(i+1));
				continue;
			}
			if (string.startsWith("预借现金额度")){
				map.put("预借现金额度", listString.get(i+1));
				continue;
			}
			if (string.startsWith("本期应还金额")){
				map.put("本期应还金额", listString.get(i+1));
				map.put("本期应还金额$", listString.get(i+2));
				continue;
			}
			if (string.startsWith("最低还款额")){
				map.put("最低还款额", listString.get(i+1));
				map.put("最低还款额$", listString.get(i+2));
				continue;
			}
			if (string.startsWith("人民币账户")){
				break;
			}
		}
		return map;
	}
	
	/**
	 * 人民币账户的一些账单信息：
	 * 		ii.	本期账单余额 = 上期对账单金额 – 本期已还款金额  + 本期新增账单款  + 本期调整金额  + 循环利息
	 * 
	 * @param listString
	 * @param map
	 */
	private Map<String, String> fetchKeyMapRBMAndUSD(List<String> listString, Map<String, String> map) {
		int size = listString.size();
		for (int i = 0; i < size; i++){
			if (listString.get(i).startsWith("人民币账户")){
				for (int j = i; j < size; j++){
					if (listString.get(j).startsWith("本期账单余额")){
						map.put("本期账单余额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("上期对账单金额")){
						map.put("上期对账单金额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期已还款金额")){
						map.put("本期已还款金额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期新增账款")){
						map.put("本期新增账款", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期调整金额")){
						map.put("本期调整金额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("循环利息")){
						map.put("循环利息", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("美元账户")){
						break;
					}
				}
			}
			if (listString.get(i).startsWith("美元账户")){
				for (int j = i; j < size; j++){
					if (listString.get(j).startsWith("本期账单余额")){
						map.put("本期账单余额$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("上期对账单金额")){
						map.put("上期对账单金额$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期已还款金额")){
						map.put("本期已还款金额$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期新增账款")){
						map.put("本期新增账款$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期调整金额")){
						map.put("本期调整金额$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("循环利息")){
						map.put("循环利息$", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("积分有礼")){
						break;
					}
				}
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
	private Map<String, String> fetchCredits(List<String> listString, Map<String, String> map) {
		
		int size = listString.size();
		for (int i = 0; i < size; i++){
			if (listString.get(i).startsWith("积分有礼")){
				for (int j = i; j < size; j++){
					if (listString.get(j).startsWith("本期账单积分余额")){
						map.put("本期账单积分余额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("上期积分余额")){
						map.put("上期积分余额", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期新增积分")){
						map.put("本期新增积分", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期奖励积分")){
						map.put("本期奖励积分", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期调整积分")){
						map.put("本期调整积分", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("本期兑换积分")){
						map.put("本期兑换积分", listString.get(j+11));
						continue;
					}
					if (listString.get(j).startsWith("人民币交易")){
						break;
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * 对交易详细信息的提取：
	 * 			i.	交易日 、记账日 、交易摘要 、交易金额 、卡号后四位
	 * @param listString
	 * @param colums
	 * @return
	 */
	private List<List<String>> tradingDetailList(List<String> listString, int colums) {
		List<List<String>> list = new ArrayList<List<String>>(); 	//用于存放交易详细数据
		int size2 = listString.size();
		for(int ji = 0; ji < size2; ji++){
			if (listString.get(ji).startsWith("交易日")){		//截取人民币交易的详细内容
				int siz = size2-colums;
				for (int ij = ji; ij < siz; ij++){
					if (listString.get(ij).startsWith("美元交易")){
						System.out.println("人民币交易及详细内容List已完结！");
						if (listString.get(ij).startsWith("美元交易")){
							for(int j = ij; j < size2; j++){
								if (listString.get(j).startsWith("交易日")){
									for (int i = j ; i < siz; i++){
										if (listString.get(i).startsWith("说明 Instruction")){
											System.out.println("美元交易及详细内容List已完结！");
											break;
										}
										List<String> tempList1 = new ArrayList<String>();
										for(int a = 0; a < colums; a++){
											int cou = i + a;
											tempList1.add(listString.get(cou));
										}
										i += colums - 1;
										list.add(tempList1);
									}
									break;
								}
							}
						} 
						break;
					}
					List<String> rowInfo = new ArrayList<String>();//临时存储数据
					for(int a = 0; a < colums; a++){
						int cou = ij + a;
						rowInfo.add(listString.get(cou));
					}
					ij += colums - 1;
					list.add(rowInfo);
				}
				break;
			} 
		}
		return list;
	}

}
