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
 * 民生银行账单解析
 * 		
 * @author admin
 *			----2014/5/16
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CMSBBlank implements IBankTemplateService {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CMSBBlank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CMSB;
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException {
		
		boolean update = false;
		//如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		
		List<String> listString = new ArrayList<String>();	//存储所有数据
		Map map = new HashMap<String, String>();//抓取值
		listString.clear();
		int flag = 0;
		Long[] billId = null;
		
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CMSBBlank.class, flag});
		//第一种账单信息解析
		if (flag == 1){
			//信用卡账户信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			Map<String, String> fetchKeyMapAccount = fetchKeyMapAccount(listString,map);
			Map<String, String> fetchCredits = fetchCredits(listString, map);
			List<List> list = accountDetailsList(listString, 5);
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CMSB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CMSB);//招行旧版账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setSenderUrl(senderUrl);//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(fetchKeyMap.get("本期账单日")));
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(fetchKeyMap.get("本期账单日")),"yyyyMM"));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMap.get("本期最后还款日")));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还款金额"))));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期最低还款金额 "))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.toNumberic(fetchCredits.get("累计积分余额"))));
			billCycleInfoEntity.setCardEndOfFour(list.get(2).get(4).toString());
			billCycleInfoEntity.setUserName(Money.toNumberic(fetchKeyMap.get("姓名")));
			if (!StringUtils.isBlank(fetchKeyMap.get("性别"))){
				billCycleInfoEntity.setUserGender(Money.toNumberic(fetchKeyMap.get("性别")));
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
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMapAccount.get("上期账单金额"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(fetchKeyMapAccount.get("本期账单金额"))));
			balanceDetailEntity.setInterest(NumberTools.createDouble(Money.toNumberic(fetchKeyMapAccount.get("循环利息"))));
			balanceDetailEntity.setAdjustment(NumberTools.createDouble(Money.toNumberic(fetchKeyMapAccount.get("本期调整金额"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMapAccount.get("本期已还金额"))));
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("本期应还款金额"))));
			balanceDetailServiceImpl.save(balanceDetailEntity);
			
			billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setUsePoints(NumberTools.toInt(fetchCredits.get("累计积分余额")));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(fetchCredits.get("本期已兑换积分")));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(fetchCredits.get("本期新增积分")));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(fetchCredits.get("本期调整积分")));
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(fetchCredits.get("上期账单积分")));
			integrationDetailEntity.setAwardPoints(NumberTools.toInt(fetchCredits.get("活动奖励积分")));
			integrationDetailEntity.setTourismPoints(NumberTools.toInt(fetchCredits.get("旅游预订积分")));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			if (update)
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			int size = list.size();
			for (int i = 0; i < size; i++){
				if (i <= 1){
					continue;
				}
				List<String> dateList = list.get(i);
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
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(getDate(fetchKeyMap.get("本期账单日"), dateList, -1)));
					}
					String dateStr2 = getDate(fetchKeyMap.get("本期账单日"), dateList, j);
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateStr2));
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
	 * 获取字符串list
	 * @param parse 未解析的字符串
	 * @param listString 存放字符串list
	 * @param flag 类型标志
	 * @return 返回账单类型的标志
	 */
	private int getStringList(List<String> parse, List<String> listString,
			int flag) {
		String replace;
		int size = parse.size();
		for (int i = 0; i < size; i++){
			if (parse.get(i).contains("尊敬的")){	
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				//详细内容的提取
				listString.add(replace);
			}
			if (parse.get(i).contains("您的信用卡账户信息")){
				for (int j = i; j < size; j++){
					//详细内容的提取
					listString.add(parse.get(j)); 		//将需要的数据添加到listString里
					if (parse.get(j).contains("账单说明：")){
						flag = 1;
						break;
					}
				}
			}
		}
		return flag;
	}
	
	/**
	 * 将现有的0903等日期补全为yyyy年mm月dd日
	 * 
	 * @param formatDate
	 * @param dateList
	 * @param j
	 * @return	
	 */
	private String getDate(String formatDate, List<String> dateList, int j) {
		String str = formatDate.substring(0, 4);
		String dateStr2 = dateList.get(j+1);
		String substring = dateStr2.substring(0, 2);
		String substring1 = dateStr2.substring(3, 5);
		dateStr2 = str+"年"+substring+"月"+substring1+"日";
		return dateStr2;
	}

	/**
	 * 信息头部的抓取
	 * 		本期账单日 、本期最后还款日 、账户名称 、本期应还款金额 、本期最低还款金额 、累计积分
	 * @param listString
	 * @param map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> fetchKeyMap(List<String> listString, Map map) {
		
		String string ;
		int size = listString.size();
		for(int i = 0 ; i < size; i ++){
			string = listString.get(i);
			if (string.contains("尊敬的")){
				String substring = string.substring(3, string.length()-3);
				int indexOf = string.indexOf(":");
				String substring2 = string.substring(indexOf-3, string.length());
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
		
		map.put("本期账单日", listString.get(listString.indexOf("本期账单日")+2));
		map.put("本期最后还款日", listString.get(listString.indexOf("本期最后还款日")+2));
		
		map.put("账户名称", listString.get(listString.indexOf("账户名称")+8)+";"+listString.get(listString.indexOf("账户名称")+9));
		map.put("本期应还款金额", listString.get(listString.indexOf("本期应还款金额")+9));
		map.put("本期最低还款金额 ", listString.get(listString.indexOf("本期最低还款金额")+9));
		map.put("累计积分 ", listString.get(listString.indexOf("累计积分")+9));
		return map;
		
	}
	
	/**
	 * 人民币/美元账户
	 * 		
	 * 		b)	上期账单金额 、本期已还金额 、本期账单金额 、本期调整金额 、循环利息
	 * @param listString
	 * @param map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> fetchKeyMapAccount(List<String> listString, Map map) {
		
		map.put("上期账单金额", listString.get(listString.indexOf("上期账单金额")+2));
		map.put("本期已还金额", listString.get(listString.indexOf("本期已还金额")+2));
		map.put("本期账单金额", listString.get(listString.indexOf("本期账单金额")+2));
		map.put("本期调整金额", listString.get(listString.indexOf("本期调整金额")+2));
		map.put("循环利息", listString.get(listString.indexOf("循环利息")+2));
		return map;
	}

	/**
	 * 	本账户积分明细
	 * 		a)	累计积分余额 、上期账单积分 、本期已兑换积分 、本期新增积分 、本期调整积分 、活动奖励积分 、旅游预订积分
	 * @param listString
	 * @param map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> fetchCredits(List<String> listString, Map map) {
		
		map.put("累计积分余额", listString.get(listString.indexOf("累计积分余额")+2));
		map.put("上期账单积分", listString.get(listString.indexOf("上期账单积分")+2));
		map.put("本期已兑换积分", listString.get(listString.indexOf("本期已兑换积分")+2));
		map.put("本期新增积分", listString.get(listString.indexOf("本期新增积分")+2));
		map.put("本期调整积分", listString.get(listString.indexOf("本期调整积分")+2));
		map.put("活动奖励积分", listString.get(listString.indexOf("活动奖励积分")+2));
		map.put("旅游预订积分", listString.get(listString.indexOf("旅游预订积分")+2));
		return map;
	}
	
	/**
	 * 账户交易明细
	 * 		c)	交易日 、 记账日 、交易摘要 、交易金额 、卡号末四位
	 * @param listString
	 * @param i
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<List> accountDetailsList(List<String> listString, int size) {

		List<List> list = new ArrayList<List>(); 	//用于存放交易详细数据
		int size2 = listString.size();
		for(int i = 0; i < size2; i++){
			if (listString.get(i).equals("交易日")){		//截取人民币交易的详细内容
				int j2 = size2-size;
				for (int j = i; j < j2; j++){
					if (listString.get(j).contains("本账户积分明细")){
						System.out.println("交易及详细内容List已完结！");
						break;
					}
					List<String> tempList = new ArrayList<String>();//临时存储数据
					for(int a = 0; a < size; a++){
						int cou = j + a;
						tempList.add(listString.get(cou));
					}
					j += size - 1;
					list.add(tempList);
				}
				break;
			} 
		}
		return list;
	}

}
