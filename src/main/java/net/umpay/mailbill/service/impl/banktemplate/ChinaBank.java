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
import net.umpay.mailbill.util.string.ReadProperty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 对中国银行电子账单的解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ChinaBank implements IBankTemplateService {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChinaBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CHINA;
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
		
		List<String> listString = new ArrayList<String>();	// 存储所有数据
		Map<String,String> billMapInfo = new HashMap<String, String>(); // 抓取值
		listString.clear();
		// 辨别账单类型
		int flag = getStringList(parse, listString);
		log.info("calss:{} \tbankType:{}", new Object[]{ChinaBank.class, flag});
		// 第一种账单
		if (flag == 1){
			billMapInfo.clear();
//			log.info("china bank detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			// 1.抓取头部重要信息
			this.fetchKeyMap(listString, billMapInfo);
			// 2.积分信息抓取
			this.fetchCredits(listString, billMapInfo);
			// 3.交易详细存放在二维数组
			List<List<String>> chinaDetailList = accountDetailsList(listString, 5, billMapInfo);
			// 4.将卡号从交易描述中提取出来, 将detailList集合的5列变为6列(卡号单列出来)
			List<List<String>> detailList = this.handelDetailList(chinaDetailList);
			// 处理多卡号问题, 将多卡号分组归类
			Map<String, List<List<String>>> oneOrMoreCardMap = null;
			if (CollectionUtils.isNotEmpty(detailList)) {
				oneOrMoreCardMap = new HashMap<String,List<List<String>>>(); // 初始化
				int length = detailList.size();
				for (int k = 0; k < length; k++) {
					List<String> rowInfo = detailList.get(k);
					if(CollectionUtils.isNotEmpty(rowInfo) && oneOrMoreCardMap.containsKey(rowInfo.get(5))){
						oneOrMoreCardMap.get(rowInfo.get(5)).add(rowInfo); // 将当前行追加到对应key值,value集合后
					} else {
						List<List<String>> cardOfDetailList = new ArrayList<List<String>>();
						cardOfDetailList.add(rowInfo);
						String key = StringUtils.isBlank(rowInfo.get(5)) ? "" : rowInfo.get(5);
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
		
		return "00";
	}

	// 将卡号从交易描述中提取出来, 将detailList集合的5列变为6列(卡号单列出来)
	private List<List<String>> handelDetailList(List<List<String>> detailList){
		List<List<String>> newList = null;
		if(CollectionUtils.isNotEmpty(detailList)){
			newList = new ArrayList<List<String>>();
			int length = detailList.size();
			for(int k = 0; k < length; k++){
				if(k < 1){ // 跳过表头信息
					continue;
				}
				List<String> oldRow = detailList.get(k);
				if(CollectionUtils.isNotEmpty(oldRow)){
					String detail = oldRow.get(2); // 交易描述列, 卡号:XXX
					if(StringUtils.isNotBlank(detail) && detail.indexOf(":") != -1){
						String crad_no = detail.split(":")[0];
						oldRow.add(crad_no); // 卡号末四位,追加到第6列
					} else {
						oldRow.add(""); // 空卡号,追加到第6列
					}
					
					newList.add(oldRow); // 将重新组合的行数据,添加到二维数组
				}
			}
		} 
		
		return newList;
	}
	
	// 存储各卡相关信息
	private void handleMoreCardBill(String senderUrl, String[] oldHTML, String[] newHtml, 
			Long[] id, Long accountId, Long scVersion, Map<String, String> map,
			List<List<String>> detailList, String card_no) throws MailBillException{
		boolean update = false;
		// 如果id存在的话则更新数据
		if (id.length != 0){
			update = true;
		}
		Long[] billId = null;
		// 4.账单周期
		BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
		billCycleInfoEntity.setAccountId(accountId);
		billCycleInfoEntity.setScVersion(scVersion);
		billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CHINA);
		billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH); // 月账单
		billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
		billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CHINA);
		billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2)); // 获取发件人地址
		billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
		billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
		billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
		billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
		billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
		billCycleInfoEntity.setBillDate(DateUtil.string2Day(map.get("账单日期")));
		billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.stringToDate(map.get("账单日期")),"yyyyMM"));
		billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(map.get("到期还款日")));
		billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(map.get("信用额度"))));
		billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(map.get("最低还款额")))); // rmb
		billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(Money.toNumberic(map.get("本期积分余额"))));
		billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(map.get("本期余额")))); // 本期应还款
		billCycleInfoEntity.setCardEndOfFour(card_no);
		billCycleInfoEntity.setUserName(map.get("姓名"));
		if (!StringUtils.isBlank(map.get("性别"))){
			billCycleInfoEntity.setUserGender(map.get("性别"));
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
		
		// 5.本期应还总额详情
		BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
		if (update){
			billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
			balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
		}
		balanceDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(map.get("上期账单金额"))));
		balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(map.get("支出总计"))));
		balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(map.get("存入总计"))));
		balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(map.get("本期余额"))));
		balanceDetailServiceImpl.save(balanceDetailEntity);
		
		// 6.积分详情
		IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
		if (update){
			billId = detailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
			integrationDetailEntity = detailServiceImpl.getId(billId[0]);
		}
		integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
		integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
		integrationDetailEntity.setUsePoints(NumberTools.toInt(map.get("本期积分余额")));
		integrationDetailEntity.setAddedPoints(NumberTools.toInt(map.get("本期累计积分")));
		integrationDetailEntity.setBalancePoints(NumberTools.toInt(map.get("上月积分余额")));
		integrationDetailEntity.setAwardPoints(NumberTools.toInt(map.get("本期赢取积分")));
		integrationDetailEntity.setExchangePoints(NumberTools.toInt(map.get("本期兑换积分")));
		detailServiceImpl.save(integrationDetailEntity);
		if(update) {
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
		}
		
		// 7.交易明细列表
		if (CollectionUtils.isNotEmpty(detailList)) {
			int size = detailList.size();
			for (int i = 0; i < size; i++){
				List<String> rowInfo = detailList.get(i);
				if (CollectionUtils.isNotEmpty(rowInfo)){
					int rowLength = rowInfo.size();
					for (int j = 0; j < rowLength;){
						BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
						if (update){
							bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
							bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
						}
						bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
						bankMonthDetailEntity.setCardEndOfFour(card_no); // 卡号末四位
						bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j), "yyyy-MM-dd"));
						bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1), "yyyy-MM-dd"));
						bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+2));
						bankMonthDetailEntity.setCurrencyType(map.get("账户类型"));
						String strInput = rowInfo.get(j+3);
						if (!strInput.contains("&amp;nbsp;")){ // 或者  ";nbsp;"   
							bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME); // 收入
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
						}else{
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(rowInfo.get(j+4))));
						}
						bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
						j += rowLength;
					}
				}
				
			}
		}
	}
	
	/**
	 * 获取字符串List
	 * 
	 * @param parse	未经删改的字符串
	 * @param listString	存放字符串
	 * @param flag	账单类别
	 * @return	截取后的字符串List
	 */
	private int getStringList(List<String> parse, List<String> listString) {
		int flag = 0;
		boolean ifXh = true;
		String replace;
		if(CollectionUtils.isNotEmpty(parse)){
			int length = parse.size();
			for (int i = 0; i < length; i++){
				if (parse.get(i).contains("先生")){	
					replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
					listString.add(replace);
				}
				if (parse.get(i).startsWith("还款存根")){
					for (int j = i; j < length; j++){
						listString.add(parse.get(j));
						//if (parse.get(j).startsWith("注：")){
						if (parse.get(j).startsWith("账单信息")){
							flag = 1;
							ifXh = false;
							break;
						}
					}
				}
				// 外层循环break
				if(ifXh == false){
					break;
				}
			}
		}
		
		return flag;
	}
	
	/**
	 *	信息的抓取
	 * 		a)	信用卡号 、 账单日期 、到期还款日 、 RMB本期余额\最低还款额
	 * 		a)	账户类型 、信用额度 、可用余额 、分期可用余额 、账单日期 、到期还款日
	 * 		b)	账户类型 、上期账单金额 +支出总计 -存入总计 =本期余额 、最低还款额
	 * @param listString
	 * @param map
	 */
	private void fetchKeyMap(List<String> listString, Map<String, String> map) {
		int size = listString.size();
		for(int i = 0 ; i < size; i ++){
			String string = listString.get(i);
			if (string.contains("先生")){
				String substring = string.substring(0, string.length()-2);
				String substring2 = string.substring(string.length()-2, string.length());
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
		
		map.put("信用卡号", listString.get(listString.indexOf("信用卡号")+1));
		map.put("账单日期", listString.get(listString.indexOf("账单日期")+1));
		map.put("到期还款日", listString.get(listString.indexOf("到期还款日")+1));
		map.put("RMB本期余额\\最低还款额", listString.get(listString.indexOf("RMB本期余额\\最低还款额&nbsp;")+1));
		
		map.put("账户类型", listString.get(listString.indexOf("账户类型")+12));
		map.put("信用额度", listString.get(listString.indexOf("信用额度")+11));
		map.put("可用余额", listString.get(listString.indexOf("可用余额")+10));
		map.put("分期可用余额", listString.get(listString.indexOf("分期可用余额")+9));
		
		map.put("账户类型", listString.get(listString.indexOf("账户类型")+12));
		map.put("上期账单金额", listString.get(listString.indexOf("上期账单金额")+14));
		map.put("支出总计", listString.get(listString.indexOf("支出总计")+13));
		map.put("存入总计", listString.get(listString.indexOf("存入总计")+12));
		map.put("本期余额", listString.get(listString.indexOf("本期余额")+11));
		map.put("最低还款额", listString.get(listString.indexOf("最低还款额")+7));
	}

	/**
	 * 积分抓取
	 * 		a)	序号 、上月积分余额 、本期累计积分 、本期赢取积分 、本期兑换积分 、本期积分余额 、到期日
	 * @param listString
	 * @param map
	 */
	private void fetchCredits(List<String> listString, Map<String, String> map) {
		if(listString.indexOf("序号") != -1){ // 目前是两种账单,有的无积分
			map.put("序号", listString.get(listString.indexOf("序号")+14));
			map.put("上月积分余额", listString.get(listString.indexOf("上月积分余额")+13));
			map.put("本期累计积分", listString.get(listString.indexOf("本期累计积分")+12));
			map.put("本期赢取积分", listString.get(listString.indexOf("本期赢取积分")+11));
			map.put("本期兑换积分", listString.get(listString.indexOf("本期兑换积分")+10));
			map.put("本期积分余额", listString.get(listString.indexOf("本期积分余额")+9)); // TODO 有的账单抓的不对
			map.put("到期日", listString.get(listString.indexOf("到期日")+8));
		}
		
	}

	/**
	 * 交易明细
	 * 		a)	账户类型 、交易日期 、记账日期 、卡号 、交易描述 、存入金额 、支出金额
	 * @param listString
	 * @param i
	 * @return
	 */
	private List<List<String>> accountDetailsList(List<String> listString, int size, Map<String, String> map) {
		
		List<List<String>> detailList = new ArrayList<List<String>>();
		
		if (CollectionUtils.isNotEmpty(listString)) {
			int length = listString.size();
			for(int i = 0; i < length; i++){
				if (listString.get(i).startsWith("账户")){
					int index = i+16;
					map.put("账户", Integer.toString(index));
				}
				if (listString.get(i).startsWith("交易日期")){
					for (int s = i; s < length; s++){
						if (listString.get(s).startsWith("交易日期")){	
							List<String> temp = new ArrayList<String>();
							for (int p = 0; p < 14; p++){
								temp.add(listString.get(p+s));
							}
							detailList.add(temp);
							s += 14;
						}
						if (listString.get(s).equals("RMB")){
							int j2 = length-size;
							for (int j = s+1; j < j2; j++){
								if (listString.get(j).startsWith("积分奖励计划") || listString.get(j+1).startsWith("积分奖励计划")
										|| listString.get(j).startsWith("账单信息") || listString.get(j+1).startsWith("账单信息")){
									break;
								}
								//将交易详细信息存放在二维数组内
								List<String> rowInfo = new ArrayList<String>();
								for (int k = 0; k < size; k++){
									int tmp = k + j;
									rowInfo.add(listString.get(tmp));
								}
								j += size - 1;
								detailList.add(rowInfo);
							}
							break;
						}
					}
					break;
				}
			}
		}
		
		return detailList;
	}

	/**
	 * 此处将单元格内容为空的替换为空"&nbsp;"以此来方便区分收入与支出
	 * @param document
	 */
	public void replaceTdOfNull(Document document) {
		String text ;
		Elements elementsByTag = document.getElementsByTag("td");
		for(org.jsoup.nodes.Element e : elementsByTag){
			text = e.text();
			if (StringUtils.isBlank(text)){
				e.text("&nbsp;");
			}
		}
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
