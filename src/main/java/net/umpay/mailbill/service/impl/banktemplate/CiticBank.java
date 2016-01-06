package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 中信银行信用卡账单的解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CiticBank implements IBankTemplateService {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CiticBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CITIC;
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
			String[] newHtml, Long[] idCard, Long accountId, Long scVersion) throws MailBillException {
		
		boolean update = false;
		// 如果id存在的话则更新数据
		if (idCard.length != 0){
			update = true;
		}
		List<String> listString = new ArrayList<String>();
		Map<String,String> map = new HashMap<String, String>();
		Long[] billId = null;
		// 标志
		int flag = getStringList(parse, map, listString);
		log.info("calss:{} \tbankType:{}", new Object[]{CiticBank.class, flag});
		// 第一种账单类型
		if (flag == 1){
//			log.info("中信 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 抓取头部重要信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, map);
			// 抓取积分信息
			List<List<String>> integration = fetchCredits(listString, map);
			// 抓取账户变动内的信息
			List<List<String>> changeMapInfo = fetchKeyChangeMap(listString, map);
			// 抓取交易详细信息
			List<List<String>> detailList = accountDetailsList(listString, 6);
			int p = 0;
			int size = changeMapInfo.size();
			for (int k = 0 ; k < size; k++){
				BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
				billCycleInfoEntity.setAccountId(accountId);
				billCycleInfoEntity.setScVersion(scVersion);
				billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CITIC);
				billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
				billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
				billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CITIC);
				billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2));//获取发件人地址
				billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
				billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
				billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
				billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
				billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
				billCycleInfoEntity.setBillDate(DateUtil.string2Day(fetchKeyMap.get("账单周期结束")));
				billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMap.get("最后还款日")));
				billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.getNumber(fetchKeyMap.get("信用额度"))));
				billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.getNumber(changeMapInfo.get(k).get(5))));
				billCycleInfoEntity.setCashRmbAdvanceLimit(NumberTools.toLong(Money.getNumber(fetchKeyMap.get("预借现金额度"))));
				billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(getNumber(changeMapInfo.get(k).get(6))));//rmb
				billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.getNumber(integration.get(k).get(5))));
				billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(fetchKeyMap.get("账单周期开始")));//周期的拆分
				billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(fetchKeyMap.get("账单周期结束")));// 周期的拆分
				billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(fetchKeyMap.get("账单周期结束"),"yyyy年MM月dd日"), "yyyyMM"));// 周期的拆分
				billCycleInfoEntity.setCardEndOfFour(detailList.get(p).get(2).toString());
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
					billCycleInfoEntity.setId(idCard[0]);
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}else{
					billCycleInfoServiceImpl.save(billCycleInfoEntity);
				}
				
				billJobServiceImpl.saveJob(billCycleInfoEntity);
				
				Long id = billCycleInfoEntity.getId();
				List<String> list2 = changeMapInfo.get(k);
				//int size2 = list2.size();
				// 本期应还总额
				//for (int j = 0 ; j < size2; j++){
				BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
				if (update){
					billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
					balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
				}
				balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
				balanceDetailEntity.setBillCyclePkId(id);
				balanceDetailEntity.setBalance(NumberTools.createDouble(Money.getNumber(list2.get(2))));
				balanceDetailEntity.setPayment(NumberTools.createDouble(Money.getNumber(list2.get(3))));
				balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.getNumber(list2.get(4))));
				balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.getNumber(list2.get(5))));
				balanceDetailServiceImpl.save(balanceDetailEntity);
					//break;
				//}
				//积分明细开始
				IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
				if (update){
					billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
					integrationDetailEntity = detailServiceImpl.getId(billId[0]);
				}
				integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
				integrationDetailEntity.setBillCyclePkId(id);
				integrationDetailEntity.setBalancePoints(NumberTools.toInt(Money.getNumber(integration.get(k).get(1))));
				integrationDetailEntity.setAddedPoints(NumberTools.toInt(Money.getNumber(integration.get(k).get(2))));
				integrationDetailEntity.setAwardPoints(NumberTools.toInt(Money.getNumber(integration.get(k).get(3))));
				integrationDetailEntity.setExchangePoints(NumberTools.toInt(Money.getNumber(integration.get(k).get(4))));
				integrationDetailEntity.setUsePoints(NumberTools.toInt(Money.getNumber(integration.get(k).get(5))));
				detailServiceImpl.save(integrationDetailEntity);
				//交易日 、 银行记账日 、 卡号后四位 、 交易描述 、 交易货币/金额 、 记账货币/金额
				List<String> rowInfo = null;
				if (update){
					billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
				}
				
				// 交易明细
				if(CollectionUtils.isNotEmpty(detailList)){
					int length = detailList.size();
					for (int i = p; i < length; i++){
						if (p == 0){
							rowInfo = detailList.get(i);
							if(CollectionUtils.isNotEmpty(rowInfo)){
								int rowLength = rowInfo.size();
								if (rowLength == 1){
									p = i+1;
									break;
								}
								for (int j = 0; j < rowLength; ){
									BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
									if (update){
										bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
										bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
									}
									bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
									bankMonthDetailEntity.setBillCyclePkId(id);
									bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyyMMdd"));
									bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyyMMdd"));
									bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+2));
									bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3));
									bankMonthDetailEntity.setCurrencyType(rowInfo.get(j+4));
									String strInput = rowInfo.get(j+5);
									if (strInput.contains("-")){
										bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
									}
									bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
									bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
									j += rowLength;
								}
							}
						}
						if (k != 0){
							rowInfo = detailList.get(i);
							if(CollectionUtils.isNotEmpty(rowInfo)){
								int rowLength2 = rowInfo.size();
								for (int j = 0; j < rowLength2; ){
									BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
									if (update){
										bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
										bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
									}
									bankMonthDetailEntity.setIsMaster(MailBillTypeConstants.BANK_MAIN_CARD);
									bankMonthDetailEntity.setBillCyclePkId(id);
									bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyyMMdd"));
									bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyyMMdd"));
									bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+2));
									bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3));
									bankMonthDetailEntity.setCurrencyType(rowInfo.get(j+4));
									String strInput = rowInfo.get(j+5);
									if (strInput.contains("-")){
										bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
									}
									bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.getNumber(strInput)));
									bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
									j += rowLength2;
								}
							}
						}
					}
				}
			}
			
			return "1";
		}
		
		return "00";
	}


	private int getStringList(List<String> parse, Map<String, String> map, List<String> listString) {
		int flag = 0;
		String replace;
		
//		for(int m = 0; m < parse.size(); m++){
//			log.info(m+"parse=="+parse.get(m));
//		}
		int size = parse.size();
		for (int i = 0; i < size; i++){
			if (parse.get(i).contains("尊敬的")){
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				String sub1 = replace.substring(3, replace.length()-3);
				String sub2 = replace.substring(replace.length()-3, replace.length());
				map.put("姓名", sub1);
				if(sub2.contains("女士")){
					map.put("性别", "女");
				}else if (sub2.contains("先生")){
					map.put("性别", "男");
				}else{
					map.put("性别", "");
				}
				listString.add(replace);
				for (int j = i; j < size; j ++){
					int indexOf = parse.get(j).indexOf("-");
					if (-1 != indexOf){
						String substring = parse.get(j).substring(indexOf-11, indexOf);
						String substring2 = parse.get(j).substring(indexOf+1, indexOf+12);
						map.put("账单周期开始", substring);
						map.put("账单周期结束", substring2);
					}
					if (parse.get(j).contains("最后还款日")){
						break;
					}
				}
			}
			
			if (parse.get(i).contains("最后还款日")){
				for (int j = i; j < size; j++){
					replace = parse.get(j).replace("\\s", "").replace("&nbsp;", "");
					listString.add(replace);
					if (replace.contains("【温馨提示】")){
						flag = 1;
						break;
					}
				}
				return flag;
			}
		}
		
		return flag;
	}
	
	/**
	 * 获取字符串中的数字
	 * @param str
	 * @return String
	 */
	public String getNumber(String str){
		String regex = "\\d*";
		Pattern p = Pattern.compile(regex);
		String dou = null;
		Matcher m = p.matcher(str);

		while (m.find()) {
			if (!"".equals(m.group())){
				if (!StringUtils.isBlank(dou)){
					dou += "."+ m.group();
					break;
				}
				dou = m.group();
			}
		}
		return dou; 
	}
	
	/**
	 * 头部重要信息的抓取
	 * 		g)	本期应还款额  、最低还款额 、信用额度 、预借现金额度 、 最后还款日、账单周期
	 * 最后还款日
	 * @param listString
	 * @param map
	 */
	private Map<String, String> fetchKeyMap(List<String> listString, Map<String,String> map) {
		//类似的信息有多处相同，无法全局抓取，然后截取部分数据抓取信息
		int size = listString.size();
		for (int i = 0; i < size; i++){
			String string = listString.get(i);
			if (string.contains("最后还款日")){
				map.put("最后还款日", string.substring(6, string.length()));
				continue;
			}
			String string2 = listString.get(i+9);
			if (listString.get(i).startsWith("信用额度")){
				if (listString.get(i+10).equals("USD")){
					String value = listString.get(i+12)+":"+(listString.get(i+13).contains("元") ? listString.get(i+13).substring(0, listString.get(i+13).length()-1) : listString.get(i+13));
					map.put("信用额度", value);
				}else{
					map.put("信用额度", listString.get(i+8)+":"+(string2.contains("元") ? string2.substring(0, string2.length()-1) : string2));
				}
				continue;
			}
			String string3 = listString.get(i+10);
			if (listString.get(i).startsWith("本期应还款额")){
				map.put("本期应还款额", string2+":"+ (string3.contains("元") ? string3.substring(0, string3.length()-1) : string3));
				continue;
			}
			if (listString.get(i).startsWith("最低还款额")){
				map.put("最低还款额", string2+":"+ (string3.contains("元") ? string3.substring(0, string3.length()-1) : string3));
				continue;
			}
			if (listString.get(i).startsWith("预借现金额度")){
				map.put("预借现金额度", listString.get(i+1)+":"+listString.get(i+2));
				break;
			}
		}
		return map;
	}

	/**
	 * 	账户变动信息
	 * 			a)	卡号 、上期应还款额 、上期已还款额 +本期新增金额 =本期应还款额(以上已有) 、最低还款额(以上已有)
	 * @param listString
	 * @param map
	 */
	private List<List<String>> fetchKeyChangeMap(List<String> listString, Map<String,String> map) {
		List<List<String>>	list = new ArrayList<List<String>>();
		int indexOf = listString.indexOf("卡号");
		if (listString.get(indexOf).contains("卡号")){
			indexOf += 15;
			// 图片后面有隐藏的错误代码(HKD)
			/*if(listString.get(indexOf).length() < 18){
				++indexOf;
			}*/
			
			int size = listString.size();
			for (int i = indexOf; i < size; i++){
				List<String> listTemp = new ArrayList<String>();
				for(int j = 0; j < 7; j++){
					int k = j + i;
					listTemp.add(listString.get(k));
				}
				list.add(listTemp);
				i += 7 - 1;
				if (listString.get(i+1).contains("消费走势") || listString.get(i+2).contains("消费走势")){
					break;
				}
			}
			
		}
		return list;
	}
	
	/**
	 * 	积分变动信息
	 * 			a)	卡号 [以上已获取]、上期余额 、本期新增(含本期奖励) 、 本期奖励 、 本期已用 、 可用余额 、 有效期提醒
	 * @param listString
	 * @param map
	 */
	private List<List<String>> fetchCredits(List<String> listString, Map<String,String> map) {
		List<List<String>>	list = new ArrayList<List<String>>();
		int i = listString.indexOf("上期余额");
		if (listString.get(i).contains("上期余额")){
				i += 7;
				
				// 图片后面有隐藏的错误代码
				/*if(listString.get(i).length() < 18){
					++i;
				}
				if(listString.get(i).length() < 18){
					++i;
				}*/
				
				int size = listString.size();
				for (int j = i; j < size; j++){
					List<String> rowInfo = new ArrayList<String>();
					for(int k = 0; k < 7; k++){
						int c = k + j;
						rowInfo.add(listString.get(c));
					}
					list.add(rowInfo);
					j += 7 - 1;
					if (listString.get(j+1).contains("本期账务明细") || listString.get(j+1).contains("【") || listString.get(j+1).contains("卡号")){
						break;
					}
				}
			}
		
		return list;
	}

	/**
	 * 	本期账务明细
	 * 		交易日 、 银行记账日 、 卡号后四位 、 交易描述 、 交易货币/金额 、 记账货币/金额
	 * @param listString
	 * @param size
	 * @return
	 */
	private List<List<String>> accountDetailsList(List<String> listString, int size) {
		//用于存放交易详细数据
		List<List<String>> list = new ArrayList<List<String>>(); 	
		int flag = 0;
		int size2 = listString.size();
		for(int i = 0; i < size2; i++){
			//截取人民币交易的详细内容
			if (listString.get(i).contains("交易日")){		
				i += 12;
				int j2 = size2-size;
				for (int j = i; j < j2; j++){
					if (flag == 1){
						flag = 0;
						continue;
					}
					if (listString.get(j).contains("【温馨提示】")  || listString.get(j).contains("◆卡号")){
						flag = 1;
						i = j-1;
						List<String> tempList = new ArrayList<String>();
						tempList.add("◆卡号");
						list.add(tempList);
						break;
					}
					if (listString.get(j).contains("主卡")){
						size = size+2;//RMB		1,090.00;为交易货币的一项金额，解析出来的为两列所以加2
						continue;
					}
					//将详细交易信息存放为二维数组
					List<String> rowInfo = new ArrayList<String>();//临时存储数据
					for(int a = 0; a < size; a++){
						int cou = j + a;
						rowInfo.add(listString.get(cou));
					}
					j += size - 1;
					list.add(rowInfo);
				}
			} 
		}
		return list;
	}

	
}
