package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.banktemplate.IBankTemplateService;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
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
 * 光大银行的账单解析
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CebBank implements IBankTemplateService{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(CebBank.class);
	
	@Override
	public int getBankType() {
		return MailBillTypeConstants.BANK_TYPE_CEB;
	}
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl integrationDetailServiceImpl;
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
		Map<String,String> billInfoMap = new HashMap<String, String>();//抓取值
		listString.clear();//清除之前的数据
		int flag = 0;
		Long[] billId = null;
		flag = getStringList(parse, listString, flag);
		log.info("calss:{} \tbankType:{}", new Object[]{CebBank.class, flag});
		//第一种账单实例
		if (flag == 1){
			billInfoMap.clear();
//			log.info("光大 card detail:{}", listString);
//			for(int k = 0; k < listString.size(); k++){
//				log.info(k+"=="+listString.get(k));
//			}
			
			// 1.信用卡账户信息
			Map<String, String> fetchKeyMap = fetchKeyMap(listString, billInfoMap);
			// 2.账户的详细交易信息
			List<List<String>> detailList = accountDetailsList(listString, 5);
			// 3.账单周期
			BillCycleInfoEntity billCycleInfoEntity = new BillCycleInfoEntity();
			billCycleInfoEntity.setAccountId(accountId);
			billCycleInfoEntity.setScVersion(scVersion);
			billCycleInfoEntity.setBankId(MailBillTypeConstants.BANK_TYPE_CEB);
			billCycleInfoEntity.setBillType(MailBillTypeConstants.BILL_TYPE_MONTH);//月账单
			billCycleInfoEntity.setIsBill(MailBillTypeConstants.BILL_TRUE);
			billCycleInfoEntity.setBankBillType(MailBillTypeConstants.BILL_TYPE_CEB);
			billCycleInfoEntity.setSenderUrl(ReadProperty.getEmailUrl(oldHTML[0],2));//获取发件人地址
			billCycleInfoEntity.setNewHtmlUrl(newHtml[0]);
			billCycleInfoEntity.setOldHtmlUrl(oldHTML[0]);
			billCycleInfoEntity.setNewHtmlDFS(newHtml[1]);
			billCycleInfoEntity.setOldHtmlDFS(oldHTML[1]);
			billCycleInfoEntity.setInfoSource(ReadProperty.getEmailUrl(oldHTML[0],1));
			billCycleInfoEntity.setBillDate(DateUtil.string2Day(fetchKeyMap.get("账单日")));
			billCycleInfoEntity.setAccountOfDate(DateUtil.getFormatDate(DateUtil.string2DateMonth(fetchKeyMap.get("账单日")),"yyyyMM"));
			billCycleInfoEntity.setPaymentDueDate(DateUtil.stringToDate(fetchKeyMap.get("到期还款日")));
			billCycleInfoEntity.setRmbCreditLimit(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("信用额度(人民币)"))));
			billCycleInfoEntity.setRmbIntegration(NumberTools.toLong(fetchKeyMap.get("积分余额")));
			billCycleInfoEntity.setMinRmbPayment(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("人民币最低还款额"))));
			billCycleInfoEntity.setNewRmbBalance(NumberTools.createDouble(Money.toNumberic(fetchKeyMap.get("人民币本期应还款额"))));
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int k = 1; k < length; k++) {
					if(StringUtils.isNotBlank(detailList.get(k).get(2))){
						billCycleInfoEntity.setCardEndOfFour(detailList.get(k).get(2)); // 直到找到不为空的卡号为止, 要是多卡号需解决 TODO --zzj
						break;
					} else {
						continue;
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
			if(update){
				billCycleInfoEntity.setId(id[0]);
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}else{
				billCycleInfoServiceImpl.save(billCycleInfoEntity);
			}
			
			billJobServiceImpl.saveJob(billCycleInfoEntity);
			
			// 4.积分汇总信息
			Map<String, String> fetchCredits = fetchCredits(listString, billInfoMap);
			IntegrationDetailEntity integrationDetailEntity = new IntegrationDetailEntity();
			if (update){
				billId = integrationDetailServiceImpl.getIdByBillCycleId(update, billCycleInfoEntity, billId);
				integrationDetailEntity = integrationDetailServiceImpl.getId(billId[0]);
			}
			integrationDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
			integrationDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
			integrationDetailEntity.setRevisePoints(NumberTools.toInt(fetchCredits.get("调整积分")));
			integrationDetailEntity.setExchangePoints(NumberTools.toInt(fetchCredits.get("本期兑换积分")));
			integrationDetailEntity.setAddedPoints(NumberTools.toInt(fetchCredits.get("本期新增积分")));
			integrationDetailEntity.setBalancePoints(NumberTools.toInt(fetchCredits.get("期初积分余额")));
			integrationDetailEntity.setUsePoints(NumberTools.toInt(fetchCredits.get("本期积分余额")));
			integrationDetailServiceImpl.save(integrationDetailEntity);
			

			if (update)
			{
				billId = bankMonthDetailServiceImpl.getBillCycleId(update, billCycleInfoEntity);
			}
				
			// 5.交易明细
			if(CollectionUtils.isNotEmpty(detailList)){
				int length = detailList.size();
				for (int i = 0; i < length; i++){
					if(i < 1){
						continue;
					}
					List<String> rowInfo = detailList.get(i);
					if(CollectionUtils.isNotEmpty(rowInfo)){
						int rowLength = rowInfo.size();
						for (int j = 0; j < rowLength;){
							BillBankMonthDetailEntity bankMonthDetailEntity = new BillBankMonthDetailEntity();
							if (update){
								bankMonthDetailEntity = bankMonthDetailServiceImpl.getId(billId[i+j]);
								bankMonthDetailEntity.setId(bankMonthDetailEntity.getId());
							}
							bankMonthDetailEntity.setBillCyclePkId(billCycleInfoEntity.getId());
							bankMonthDetailEntity.setMerchandiseDate(DateUtil.stringToDate(rowInfo.get(j)));
							bankMonthDetailEntity.setPostDate(DateUtil.stringToDate(rowInfo.get(j+1)));
							bankMonthDetailEntity.setCardEndOfFour(rowInfo.get(j+2));
							bankMonthDetailEntity.setMerchandiseDetail(rowInfo.get(j+3));
							String strInput = rowInfo.get(j+4);
							if (strInput.contains("存入")){
								bankMonthDetailEntity.setIncomeOrPay(MailBillTypeConstants.BILL_INCOME);
							}
							bankMonthDetailEntity.setAmount(NumberTools.createDouble(Money.toNumberic(strInput)));
							bankMonthDetailEntity.setCurrencyType(MailBillTypeConstants.RMB_CURRENCY_TYPE);
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

	/**
	 * 截取字符串lIst
	 * @param parse		含不需要数据的字符
	 * @param listString	截取有用的
	 * @param flag	账单类型
	 * @return	int
	 */
	private int getStringList(List<String> parse, List<String> listString, int flag) {
		String replace ;
		int size = parse.size();
		for (int i = 0; i < size; i++){
			if (parse.get(i).contains("尊敬的")){	
				replace = parse.get(i).replace("\\s", "").replace("&nbsp;", "");
				listString.add(replace);
			}
			if (parse.get(i).startsWith("中国光大银行信用卡电子账单")){
				for (int j = i; j < size; j++){
					//详细内容的提取
					listString.add(parse.get(j).replace("\\s", "").replace("&nbsp;", ""));
					if (parse.get(j).contains("精彩活动")){
						flag = 1;
						break;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 信息头部的抓取
	 * 		ii.	账单日 、到期还款日、信用额度(人民币) 、人民币本期应还款额 、人民币最低还款额 、积分余额
	 * @param listString
	 * @param map
	 * @return 
	 */
	private Map<String, String> fetchKeyMap(List<String> listString, Map<String,String> map) {
		GetUserInfoUtil.getUserInfoFor(listString, map, 3, 5);
		map.put("账单日", listString.get(listString.indexOf("账单日StatementDate")+6));
		map.put("到期还款日", listString.get(listString.indexOf("到期还款日PaymentDueDate")+6));
		map.put("信用额度(人民币)", listString.get(listString.indexOf("信用额度(人民币)CreditLimit(RMB)")+6));
		map.put("人民币本期应还款额", listString.get(listString.indexOf("人民币本期应还款额RMBCurrentAmountDue")+6));
		map.put("人民币最低还款额", listString.get(listString.indexOf("人民币最低还款额RMBMinimumAmountDue")+6));
		map.put("积分余额", listString.get(listString.indexOf("积分余额RewardsPointsBalance")+6));
		return map;
	}
	
	/**
	 * 人民币账户的一些信息
	 * 		i.	账户 、本期余额 、本期应还款额 、本期最小还款额
	 * @param listString
	 
	private List<List<String>> fetchKeyMapRBM(List<String> listString, int size) {
		List<List<String>> listRMB = new ArrayList<List<String>>();
		listRMB.clear();
		int size2 = listString.size();
		for (int i = 0; i < size2; i++){
			if (listString.get(i).startsWith("账户")){
				int j2 = size2 - size;
				for (int j = i; j < j2; j++){
					//遇到积分结束循环
					if (listString.get(j).startsWith("积分")){
						break;
					}
					//遇到总计变换存放数组的长度
					if (listString.get(j).startsWith("总计")){
						break;
					}
					//将详细交易以二维数组的形式存放
					List<String> temp = new ArrayList<String>();
					for (int k = 0; k < size; k++){
						int tmp = k + j;
						temp.add(listString.get(tmp));
					}
					j += size - 1;
					listRMB.add(temp);
				}
				break;
			}
		}
		return listRMB;
	}*/
	
	/**
	 * 抽取积分统计信息
	 * 积分“方程式”：
	 * 		i.	本期积分余额 = 期初积分余额 + 本期新增积分  - 本期兑换积分  + 调整积分
	 * @param listString
	 * @param map
	 * @return 
	 */
	private Map<String, String> fetchCredits(List<String> listString, Map<String, String> map) {
		int size = listString.size();
		for (int i = 0; i < size; i++){
			if (listString.get(i).startsWith("积分统计")){
				for (int j = i; j < size; j++){
					if (listString.get(j).startsWith("本期积分余额")){
						map.put("本期积分余额", listString.get(j+9));
						continue;
					}
					if (listString.get(j).startsWith("期初积分余额")){
						map.put("期初积分余额", listString.get(j+9));
						continue;
					}
					if (listString.get(j).startsWith("本期新增积分")){
						map.put("本期新增积分", listString.get(j+9));
						continue;
					}
					if (listString.get(j).startsWith("本期兑换积分")){
						map.put("本期兑换积分", listString.get(j+9));
						continue;
					}
					if (listString.get(j).startsWith("调整积分")){
						map.put("调整积分", listString.get(j+9));
						continue;
					}
					if (listString.get(j).startsWith("人民币账户") || listString.get(j).startsWith("注：")){
						break;
					}
				}
				break;
			}
		}
		return map;
	}
	
	/**
	 * 人民币账户交易明细
	 * 		ii.	交易日期 、记账日期 、卡号末四位 、交易说明 、金额
	 * @param listString
	 * @param colums
	 * @return
	 */
	private List<List<String>> accountDetailsList(List<String> listString, int colums) {
		List<List<String>> returnList = new ArrayList<List<String>>(); 	//用于存放交易详细数据
		if(CollectionUtils.isNotEmpty(listString)){
			int length = listString.size();
			for(int ji = 0; ji < length; ji++){
				if (listString.get(ji).startsWith("交易日")){		//截取人民币交易的详细内容
					int siz = length-colums;
					for (int ij = ji; ij < siz; ij++){
						if (listString.get(ij).startsWith("美元") || listString.get(ij).startsWith("本期欠款")
								|| listString.get(ij).startsWith("精彩活动")){
							System.out.println("人民币交易及详细内容List已完结！");
							//---------此处暂时保留，不知道以后有木有美元交易
							if (listString.get(ij).startsWith("美元")){
								for(int j = ij; j < length; j++){
									if (listString.get(j).startsWith("交易日")){
										for (int i = j ; i < siz; i++){
											if (listString.get(i).startsWith("精彩活动") 
													|| listString.get(ij).startsWith("本期欠款")){
												System.out.println("美元交易及详细内容List已完结！");
												break;
											}
											//将交易详细存放在二维数组内
											List<String> rowInfo = new ArrayList<String>();
											for(int a = 0; a < colums; a++){
												int cou = i + a;
												rowInfo.add(listString.get(cou).replace("\\s", "").replace("&nbsp;", ""));
											}
											i += colums - 1;
											returnList.add(rowInfo);
										}
										break;
									}
								}
							} 
							break;
						}
						//将交易详细存放在二维数组内
						List<String> rowInfo2 = new ArrayList<String>();
						for(int a = 0; a < colums; a++){
							int cou = ij + a;
							rowInfo2.add(listString.get(cou).replace("\\s", "").replace("&nbsp;", ""));
						}
						ij += colums - 1;
						returnList.add(rowInfo2);
					}
					break;
				} 
			}
		}
		
		return returnList;
	}
}
