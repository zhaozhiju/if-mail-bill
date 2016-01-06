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
 * 广发银行账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CgbBank implements IBankTemplateService {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CgbBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CGB;
	}
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl integrationDetailServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;
	
	/**
	 * 广发银行账单解析
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
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CgbBank.class, flag});
		if (flag == 1){
			Map<String, String> cgbAnBankInfoMap = CgbAnBankInfoMap(listString,map);
			List<List<String>> list = CgbBankInfoList(listString);
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CGB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CGB);
			billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2));//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("综合信用额度"))));//与”信用额(元)“一样
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(cgbAnBankInfoMap.get("本期最低还款")));//rmb
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(cgbAnBankInfoMap.get("还款到期日")));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("本期应还总额"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(cgbAnBankInfoMap.get("本期余额")));
			String[] split = cgbAnBankInfoMap.get("账单周期").split("-");
			billCycleInfoEntity.setBillCycleBegin(DateUtil.stringToDate(split[0]));//周期的拆分
			billCycleInfoEntity.setBillCycleEnd(DateUtil.stringToDate(split[1]));// 周期的拆分
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(split[1]));//账单日
			Date stringToDate = DateUtil.stringToDate(split[1]);
			String formatDate = DateUtil.getFormatDate(stringToDate,"yyyyMM");
			billCycleInfoEntity.setAccountOfDate(formatDate);
			String cardEndOfFour = cgbAnBankInfoMap.get("卡号");
			cardEndOfFour = cardEndOfFour.substring(cardEndOfFour.length()-4, cardEndOfFour.length());
			billCycleInfoEntity.setCardEndOfFour(cardEndOfFour);
			billCycleInfoEntity.setUserName(Money.toNumberic(cgbAnBankInfoMap.get("姓名")));
			if (!StringUtils.isBlank(cgbAnBankInfoMap.get("性别"))){
				billCycleInfoEntity.setUserGender(Money.toNumberic(cgbAnBankInfoMap.get("性别")));
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
			
			Long[] billId = null;
			billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			int size = list.size();
			for (int i = 0; i < size; i++){
				List<String> dateList = list.get(i);
				for (int j = 0; j < dateList.size();){
					BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
					if (update){
						bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
						bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
					}
					bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
					bankMonthDetailEntity.setCardEndOfFour(cardEndOfFour);
					bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(dateList.get(j), "yyyyMMdd"));
					bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(dateList.get(j+1), "yyyyMMdd"));
					bankMonthDetailEntity.setMerchandiseDetail(Money.toNumberic(dateList.get(j+2)));
					String strInput = dateList.get(j+3);
					if (strInput.contains("-")){
						bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
					}
					bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
					bankMonthDetailEntity.setCurrencyType(dateList.get(j+4));
					bankMonthDetailServiceImpl.save(bankMonthDetailEntity);
					j += 6;
				}
			}
			
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(cgbAnBankInfoMap.get("上期余额")));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(cgbAnBankInfoMap.get("本期新增")));
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(cgbAnBankInfoMap.get("本期扣减")));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(cgbAnBankInfoMap.get("本期余额")));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			
			BalanceDetailEntity balanceDetailEntity = new BalanceDetailEntity();
			if (update){
				billId = balanceDetailServiceImpl.getIdByBillCycle(update, billCycleInfoEntity, billId);
				balanceDetailEntity = balanceDetailServiceImpl.getId(billId[0]);
			}
			balanceDetailEntity.setCurrencyType(cgbAnBankInfoMap.get("清算货币"));
			balanceDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			balanceDetailEntity.setBalance(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("上期应还总额"))));
			balanceDetailEntity.setNewCharges(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("本期支出"))));
			balanceDetailEntity.setPayment(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("本期收入"))));
			balanceDetailEntity.setNewBalance(NumberTools.createDouble(Money.toNumberic(cgbAnBankInfoMap.get("本期应还总额"))));
			
			balanceDetailServiceImpl.save(balanceDetailEntity);
			return "1";
		}
		
		return "00";
	}

	/**
	 * 获取截取后的字符串数组
	 * @param parse
	 * @param listString
	 */
	private int getStringList(List<String> parse, List<String> listString, int flag) {
		String replace;
		int size = parse.size();
		for(int i = 1; i<size; i++){
			if (parse.get(i).contains("您好")){	
				replace = parse.get(i-1).replace("\\s", "").replace("&nbsp;", "");
				listString.add(replace);
			}
			if (parse.get(i).startsWith("账单周期Statement")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.startsWith("注：")){
						break;
					}
				}
			}
			if (parse.get(i).contains("交易明细")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.startsWith("温馨提示")){
						break;
					}
				}
			}
			if (parse.get(i).contains("本期应还总额情况")){	
				for (int jj = i; jj < size; jj++){
					replace = parse.get(jj).replace("\\s", "").replace("&nbsp;", "");
					//详细内容的提取
					listString.add(replace);
					if(replace.startsWith("◆")){
						break;
					}
				}
				flag = 1;
			}
		}
		return flag;
	}
	
	/**
	 * 存储广发银行解析后的一次性数据
	 * （注释掉的可能为冗余数据）
	 * 
	 * @param list 处理并过滤后的HTML内容
	 * @param map	存放需要的一次性数据
	 * @return 
	 */
	private Map<String, String> CgbAnBankInfoMap(List<String> list, Map<String, String> map){
		int size = list.size();
		for(int i = 0 ; i < size; i ++){
			String string = list.get(i);
			GetUserInfoUtil.getUserInfo(map, string, 3, 3);
			if (!StringUtils.isBlank(map.get("姓名"))){
				break;
			}
		}
			
		//信用卡账户信息开始
		map.put("账单周期", list.get(list.indexOf("账单周期Statementcycle")+1));
		map.put("综合信用额度", list.get(list.indexOf("综合信用额度TotalCreditLimit")+1));
		
		map.put("卡号", list.get(list.indexOf("综合信用额度TotalCreditLimit")+8));
		map.put("本期应还总额", list.get(list.indexOf("综合信用额度TotalCreditLimit")+9));
		map.put("本期最低还款", list.get(list.indexOf("综合信用额度TotalCreditLimit")+10));
		map.put("还款到期日", list.get(list.indexOf("综合信用额度TotalCreditLimit")+11));
		map.put("清算货币", list.get(list.indexOf("综合信用额度TotalCreditLimit")+12));
		map.put("信用额(元)", list.get(list.indexOf("综合信用额度TotalCreditLimit")+13));
		
		//交易明细为循环数据 通过CgbBankInfoList方法存储
		map.put("现金利息", list.get(list.indexOf("现金利息")+1));
		map.put("合计", list.get(list.indexOf("合计")+3));
		
		//本期应还总额情况开始
//		map.put("卡号_本期应还总额情况NewBalance", list.get(list.indexOf("本期应还总额情况NewBalance")+11)); //与上面的”卡号“的值相同
//		map.put("清算货币_本期应还总额情况NewBalance", list.get(list.indexOf("本期应还总额情况NewBalance")+12));	//与上面的”清算货币“的值相同
		map.put("上期应还总额", list.get(list.indexOf("本期应还总额情况NewBalance")+13));
		map.put("本期支出", list.get(list.indexOf("本期应还总额情况NewBalance")+15));
		map.put("本期收入", list.get(list.indexOf("本期应还总额情况NewBalance")+17));
//		map.put("本期应还总额_本期应还总额情况NewBalance", list.get(list.indexOf("本期应还总额情况NewBalance")+16)); //与上面的”本期应还总额“的值相同
		
		//积分按卡号汇总情况开始
//		map.put("卡号_积分按卡号汇总情况PointSummaryonCardNumber", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+8));	//与上面的”卡号“的值相同
		map.put("积分类型", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+9));
		map.put("上期余额", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+10));
		map.put("本期新增", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+11));
		map.put("本期扣减", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+12));
		map.put("本期余额", list.get(list.indexOf("积分按卡号汇总情况PointSummaryonCardNumber")+13));
		
		//调额信息
//		map.put("调额信息", list.get(list.indexOf("调额信息")+1));
		return map;
	}
	
	/**
	 * 存储广发银行消费明细（循环数据）
	 * 1.交易日 2.入账日 3.交易摘要 4.交易金额 5.交易货币/积分类型 6.入账金额/积分 
	 * 
	 * @param list 处理并过滤后的数据
	 * 
	 */
	private List<List<String>> CgbBankInfoList(List<String> list){
		List<List<String>> listDouble = new ArrayList<List<String>>();
		int start = list.indexOf("交易明细")+2;	//明细信息开始位置
		int end = list.indexOf("合计")-3;	//明细信息结束位置
		final int count_field = 6;	//明细字段数
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

}
