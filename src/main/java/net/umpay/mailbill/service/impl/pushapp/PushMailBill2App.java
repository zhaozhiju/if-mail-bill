package net.umpay.mailbill.service.impl.pushapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.umpay.mailbill.api.model.pushapp.Card;
import net.umpay.mailbill.api.model.pushapp.CardDetial;
import net.umpay.mailbill.api.model.pushapp.Cycle;
import net.umpay.mailbill.api.model.pushapp.CycleDetial;
import net.umpay.mailbill.api.model.pushapp.DayBill;
import net.umpay.mailbill.api.model.pushapp.DayBillDetial;
import net.umpay.mailbill.api.model.pushapp.MailsOfAccount;
import net.umpay.mailbill.api.model.pushapp.MonthBill;
import net.umpay.mailbill.api.model.pushapp.MonthBillDetial;
import net.umpay.mailbill.api.model.pushapp.UnBindingMail;
import net.umpay.mailbill.api.pushapp.IPushMailBill2App;
import net.umpay.mailbill.entrance.websocket.MyWebSocket;
import net.umpay.mailbill.hql.dao.BillBankDayDetailDao;
import net.umpay.mailbill.hql.dao.BillBankMonthDetailDao;
import net.umpay.mailbill.hql.dao.BillCycleInfoDao;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.string.EmptyUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <pre>
 *   <li>以webSocket长链接多次向客户端 发送数据形式分别推送:</li>
 *   <li>1.卡数据;2.账期数据;3.详情数据;</li>
 *   <li>目的：支持客户端的进度平滑显示</li>
 * </pre>
 * 
 * @author zhaozj add on 2014/10/29
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class PushMailBill2App extends MyWebSocket implements IPushMailBill2App {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(PushMailBill2App.class);
	
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private BillCycleInfoDao billCycleInfoDao;
	@Autowired
	private BillBankMonthDetailDao billBankMonthDetailDao;
	@Autowired
	private BillBankDayDetailDao billBankDayDetailDao;
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	
	
	
	@Override
	public void pushUnBindingMail(String emailUrl, Long accountId,
			String phoneId, String socketKey) throws MailBillException {
		StringBuffer sendData = new StringBuffer();
		UnBindingMail unBindingMail = new UnBindingMail();
		int flag = billUserInfoServiceImpl.updateToUnbundling(accountId,
				emailUrl);
		if (flag > 0) {
			unBindingMail.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP6);
			unBindingMail.setUnbindingMailFlag("true");
		} else {

			unBindingMail.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP6);
			unBindingMail.setUnbindingMailFlag("false");
		}

		String unBindingMailJson = "";

		try {
			unBindingMailJson = objectMapper.writeValueAsString(unBindingMail);
		} catch (Exception ex) {
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
		}

		sendData.append("DELETE1`");
		sendData.append(unBindingMailJson.toString());
		this.send(sendData.toString(), socketKey);
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_unbinding_mail:{}",
				new Object[] { emailUrl, accountId, phoneId, sendData.toString() });
	}
	
	
	
	@Override
	public void pushAccountOfMails(String mailUrl, Long accountId, String phoneId,
			String socketKey) throws MailBillException {
		String mailsJson = "";
		MailsOfAccount mailsOfAccount = new MailsOfAccount();
		StringBuffer sendData = new StringBuffer();
		// 获取绑定的邮箱列表
		List<String> mails = billUserInfoServiceImpl.findMailUrlList(accountId);
		Object[] objArr = mails.toArray();
		String[] mailStringArr = new String[objArr.length];
		for(int k = 0; k < objArr.length; k++){
			mailStringArr[k] = objArr[k].toString();
		}
		
		Set<String> mailsSet = new HashSet<String>(Arrays.asList(mailStringArr)); // 去重

		if (CollectionUtils.isNotEmpty(mailsSet)) {
			mailsOfAccount.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP5);
			mailsOfAccount.setMailArr(mailsSet);
			// 转json串
			try {
				mailsJson = objectMapper.writeValueAsString(mailsOfAccount);
			} catch (Exception ex) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
						ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
			}
		}

		if (StringUtils.isNotBlank(mailsJson)) {
			sendData.append("COUNT`1");
			this.send(sendData.toString(), socketKey);
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_account_conut:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
		} else {
			this.send("A1`无绑定邮箱数据", socketKey);
			return;
		}
		
		sendData.setLength(0);
		sendData.append("M1`");
		sendData.append(mailsJson);
		this.send(sendData.toString(), socketKey);
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_account_mails:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
	}
	
	/**
	 * 推送数据入口
	 * 
	 * @param mailUrl
	 *            邮箱url
	 * @param accountId
	 *            用户账号
	 * @param csVersion
	 *            客户端数据版本号
	 * @param socketKey
	 *            socket长连接的唯一标示
	 * @throws MailBillException
	 */
	
	@Override
	public void pushMailBillEntrance(String mailUrl, Long accountId, String phoneId, 
			String csVersion, String socketKey) throws MailBillException {
		int countPackage = 0; // 要传送的总数据包数量
		int indexPackage = 0; // 当前传送的第几个数据包
		Map<String, Integer> indexPMap = new HashMap<String, Integer>();
		StringBuffer sendData = new StringBuffer();

		// 0. 计算要推送的数据包的总量
		countPackage = this.getCountPackage(mailUrl, accountId, csVersion);
		sendData.append("COUNT`"); // 总的数据包数量
		sendData.append(countPackage);
		this.send(sendData.toString(), socketKey);
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_count:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
		if (countPackage == 1) {
			sendData.setLength(0);
			sendData.append("A1`无新的卡数据");
			this.send(sendData.toString(), socketKey); // 无新的卡数据
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_info:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
			return;
		}

		// 1. 推送卡数据
		Card cardInfo = this.getCardsInfo(mailUrl, accountId, csVersion);
		sendData.setLength(0);
		sendData.append("INDEX`");
		sendData.append(++indexPackage);
		this.send(sendData.toString(), socketKey);
		indexPMap.put("indexP", Integer.valueOf(indexPackage)); // 将当前传送的包数量缓存
		String cardInfoJson = "";
		try {
			cardInfoJson = objectMapper.writeValueAsString(cardInfo);
		} catch (Exception ex) {
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
		}
		sendData.setLength(0); // 清空缓存
		sendData.append("X1`"); // 卡数据
		sendData.append("&" + mailUrl + "&"); // 邮箱地址 TODO 配合测试用例, 测完要删掉
		sendData.append(cardInfoJson);
		this.send(sendData.toString(), socketKey); // 卡数据
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_card_data:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
		
		// 2. 推送卡下的月、日账单账期及详情
		List<CardDetial> cardArr = cardInfo.getCardArr();
		if (CollectionUtils.isNotEmpty(cardArr)) {
			int length = cardArr.size();
			for (int k = 0; k < length; k++) {
				CardDetial cardDetial = cardArr.get(k);
				if(Integer.parseInt(cardDetial.getBillType()) == MailBillTypeConstants.BILL_TYPE_MONTH){
					// 当前卡的[月]账单账期数据及账期下详情数据
					this.pushMonthData(cardDetial, mailUrl, accountId, phoneId, csVersion, socketKey, indexPMap);
				} else {
					// 当前卡的[日]账单账期数据及账期下详情数据
					this.pushDayData(cardDetial, mailUrl, accountId, phoneId, csVersion, socketKey, indexPMap);
				}
			}
		}
	}

	@Override
	public Card getCardsInfo(String mailUrl, Long accountId, String csVersion)
			throws MailBillException {
		// 从账期表查出数据
		List<BillCycleInfoEntity> cycleList = billCycleInfoDao.findCardsInfo(mailUrl, accountId, csVersion);
		List<CardDetial> cardDetialList = new ArrayList<CardDetial>();
		Set<String> cardSignSet = new HashSet<String>();	//用于记录银行卡标识信息
		String bank_id = "";
		String cardEndOfFour = "";
		String paymentDueDate = "";
		String userName = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		if(CollectionUtils.isNotEmpty(cycleList)){
			int cycleListSize = cycleList.size();
			for(int i = 0; i < cycleListSize; i++){
				String cardSign = "";
				CardDetial cardDetial = new CardDetial();
				BillCycleInfoEntity billCycleInfoEntity = cycleList.get(i);
				// 月账单以 "bank_id-cardEndOfFour-paymentDueDate-月账单标示" 作为标识
				if(billCycleInfoEntity.getBillType() == MailBillTypeConstants.BILL_TYPE_MONTH){
					bank_id = billCycleInfoEntity.getBankId().toString();
					cardEndOfFour = EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour());
					
					if(StringUtils.isBlank(cardEndOfFour)){ // 卡号为空, 以还款日加以区分
						if(billCycleInfoEntity.getPaymentDueDate() != null){
							paymentDueDate = sdf.format(billCycleInfoEntity.getPaymentDueDate());
							userName = billCycleInfoEntity.getUserName();
							if(StringUtils.isNotBlank(paymentDueDate) && paymentDueDate.indexOf("/") != -1){
								String[] arr = paymentDueDate.split("/");
								cardSign = bank_id + "-" +  arr[2] + "-" + userName +"-"+ MailBillTypeConstants.BILL_TYPE_MONTH;
							} else {
								cardSign = bank_id + "-" + userName +"-"+ MailBillTypeConstants.BILL_TYPE_MONTH;
							}
						}
					} else { // 卡号不为空, 以卡号末四位以区分
						cardSign = bank_id + "-" + cardEndOfFour + "-" + MailBillTypeConstants.BILL_TYPE_MONTH;
					}
				}
				// 日账单以  "bank_id-cardEndOfFour-日账单标示" 作为标识
				if(billCycleInfoEntity.getBillType() == MailBillTypeConstants.BILL_TYPE_DAY){
					bank_id = billCycleInfoEntity.getBankId().toString();
					cardEndOfFour = EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour());
					cardEndOfFour = EmptyUtil.emptyStringHandle(cardEndOfFour);
					cardSign = bank_id + "-" + cardEndOfFour + "-" + MailBillTypeConstants.BILL_TYPE_DAY;
				}
				
				//log.info("划分卡数量, cardSign:{}", cardSign);
				// 当前未发现该卡
				if(!cardSignSet.contains(cardSign)){
					// 月账单全部字段进行存储
					if(billCycleInfoEntity.getBillType() == MailBillTypeConstants.BILL_TYPE_MONTH){
						cardDetial.setBankId(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getBankId().toString()));		//银行ID
						cardDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour()));	//卡号末四位
						cardDetial.setAccountOfDate(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getAccountOfDate()));	//账期
						cardDetial.setUserName(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getUserName()));				//用户姓名
						cardDetial.setUserGender(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getUserGender()));			//用户性别
						cardDetial.setInfoSource(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getInfoSource()));			//邮箱信息来源
						cardDetial.setScVersion(billCycleInfoEntity.getScVersion()); //服务端数据版本号
						cardDetial.setBillType(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getBillType().toString()));	//账单类型
						cardDetial.setRmbCreditLimit(billCycleInfoEntity.getRmbCreditLimit()); //人民币信用额度
						cardDetial.setUsaCreditLimit(billCycleInfoEntity.getUsaCreditLimit()); //美元信用额度
						if(billCycleInfoEntity.getPaymentDueDate() != null){
							cardDetial.setPaymentDueDate(sdf.format(billCycleInfoEntity.getPaymentDueDate())); //还款日
						}else{
							cardDetial.setPaymentDueDate("");
						}
						cardDetial.setCashUsaAdvanceLimit(billCycleInfoEntity.getCashUsaAdvanceLimit()); //美元预借现金额度
						cardDetial.setCashRmbAdvanceLimit(billCycleInfoEntity.getCashRmbAdvanceLimit()); //人民币预借现金额度
						cardSignSet.add(cardSign); // 记录当前标示, 以区分卡重复
						cardDetialList.add(cardDetial);
					}
					// 日账单部分字段进行存储
					if(billCycleInfoEntity.getBillType() == MailBillTypeConstants.BILL_TYPE_DAY){
						cardDetial.setBankId(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getBankId().toString()));		//银行ID
						cardDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour()));	//卡号末四位
						cardDetial.setAccountOfDate(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getAccountOfDate()));	//账期
						cardDetial.setUserName(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getUserName()));				//用户姓名
						cardDetial.setUserGender(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getUserGender()));			//用户性别
						cardDetial.setInfoSource(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getInfoSource()));			//邮箱信息来源
						cardDetial.setScVersion(billCycleInfoEntity.getScVersion());										//服务端数据版本号
						cardDetial.setBillType(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getBillType().toString()));	//账单类型
						cardSignSet.add(cardSign); // 记录当前标示, 以区分卡重复
						cardDetialList.add(cardDetial);
					}
				}
			}
		}
		
		
		for(String str : cardSignSet){
			log.info("cardSignSet==>"+str+"<");
		}
		Card card = new Card();
		card.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP1);
		card.setCardArr(cardDetialList);
		return card;
	}

	@Override
	public Cycle geMonthtCylesInfo(String mailUrl, Long accountId,
			String csVersion, String userName, long bankId, String cardEndOfFour, String paymentDate, CardDetial cardDetial)
			throws MailBillException {
		String disPaymentDueDate = "";
		// 从账期表查出数据
		Integer billType = MailBillTypeConstants.BILL_TYPE_MONTH;
		List<BillCycleInfoEntity> cycleList = new ArrayList<BillCycleInfoEntity>();
		if(StringUtils.isNotBlank(cardEndOfFour)){ // 卡号非空的卡账期
			cycleList = billCycleInfoDao.findMonthtCylesInfo(mailUrl, accountId, csVersion, bankId, cardEndOfFour, billType);
		}else{ // 卡号为空的卡账期
//			Date _paymentStr2Date = DateUtil.stringToDate(paymentDate, "yyyy/MM/dd");
			String dd = "";
			if(StringUtils.isBlank(paymentDate) && paymentDate.indexOf("/") != -1){
				dd = paymentDate.split("/")[2];
			}
			cycleList = billCycleInfoDao.findMonthtCylesInfoNoCardNum(mailUrl, accountId, csVersion, userName, dd, bankId, billType);
		}
		List<CycleDetial> cycleDetialList = new ArrayList<CycleDetial>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		if(CollectionUtils.isNotEmpty(cycleList)){
			int cycleListSize = cycleList.size();
			for(int i = 0; i < cycleListSize; i++){
				CycleDetial cycleDetial = new CycleDetial();
				BillCycleInfoEntity billCycleInfoEntity = cycleList.get(i);
				cycleDetial.setId(billCycleInfoEntity.getId());	// 账单周期表主键
				cycleDetial.setAccountOfDate(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getAccountOfDate()));	// 账期
				cycleDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour()));	// 卡号后四位
				if(billCycleInfoEntity.getBillCycleBegin() != null){
					Date begin = billCycleInfoEntity.getBillCycleBegin();
					Date end = billCycleInfoEntity.getBillCycleEnd();
					cycleDetial.setBillCycle(sdf.format(begin)+"-"+sdf.format(end)); // 账单周期
				}else{
					cycleDetial.setBillCycle("");
				}
				if(null != billCycleInfoEntity.getPaymentDueDate()){
					disPaymentDueDate = sdf.format(billCycleInfoEntity.getPaymentDueDate());
					cycleDetial.setPaymentDueDate(disPaymentDueDate); // 到期还款日
				}else{
					cycleDetial.setPaymentDueDate(null);
				}
				cycleDetial.setNewRmbBalance(billCycleInfoEntity.getNewRmbBalance());	// 本期应还人民币总额
				cycleDetial.setNewUsaBalance(billCycleInfoEntity.getNewUsaBalance());	// 本期应还美元总额
				cycleDetial.setMinRmbPayment(billCycleInfoEntity.getMinRmbPayment());	// 本期最低人民币还款额
				cycleDetial.setMinUsaPayment(billCycleInfoEntity.getMinUsaPayment());	// 本期最低美元还款额
				cycleDetial.setInfoSource(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getInfoSource())); // 邮箱信息来源
				cycleDetial.setSubject(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getSubject())); // 邮件主题
				cycleDetialList.add(cycleDetial);
			}
		}
		Cycle cycle = new Cycle();
		cycle.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP2);
		cycle.setBankId(String.valueOf(bankId));
		cycle.setCardEndOfFour(cardEndOfFour);
		cycle.setPaymentDueDate(disPaymentDueDate);
		cycle.setBillType(String.valueOf(MailBillTypeConstants.BILL_TYPE_MONTH));
//		cycle.setCardUniqueNo(cardDetial.getCardUniqueNo()); // 卡唯一标示
		cycle.setBillcycleArr(cycleDetialList);
		return cycle;
	}

	@Override
	public Cycle getDayCylesInfo(String mailUrl, Long accountId,
			String csVersion, String userName, long bankId, String cardEndOfFour, CardDetial cardDetial)
			throws MailBillException {
		// 从账期表查出数据
		Integer billType = MailBillTypeConstants.BILL_TYPE_DAY;
		List<BillCycleInfoEntity> cycleList = billCycleInfoDao.findMonthtCylesInfo(mailUrl, accountId, csVersion, bankId, cardEndOfFour, billType);
		List<CycleDetial> cycleDetialList = new ArrayList<CycleDetial>();
		if(CollectionUtils.isNotEmpty(cycleList)){
			int cycleListSize = cycleList.size();
			for(int i = 0; i < cycleListSize; i++){
				CycleDetial cycleDetial = new CycleDetial();
				BillCycleInfoEntity billCycleInfoEntity = cycleList.get(i);
				cycleDetial.setId(billCycleInfoEntity.getId());	// 账单周期表主键
				cycleDetial.setAccountOfDate(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getAccountOfDate())); // 账期
				cycleDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getCardEndOfFour())); // 卡号后四位
				cycleDetial.setInfoSource(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getInfoSource())); // 邮箱信息来源
				cycleDetial.setSubject(EmptyUtil.emptyStringHandle(billCycleInfoEntity.getSubject())); // 邮件主题
				cycleDetialList.add(cycleDetial);
			}
		}
		Cycle cycle = new Cycle(); // 日账单无需还款日设置
		cycle.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP2);
		cycle.setBankId(String.valueOf(bankId));
		cycle.setCardEndOfFour(cardEndOfFour);
		cycle.setBillType(String.valueOf(MailBillTypeConstants.BILL_TYPE_DAY));
//		cycle.setCardUniqueNo(cardDetial.getCardUniqueNo()); // 卡唯一标示
		cycle.setBillcycleArr(cycleDetialList);
		return cycle;
	}

	@Override
	public MonthBill getMonthBillsInfo(long cyclePkId, CardDetial cardDetial) throws MailBillException {
		//从月账单详情表查出数据
		List<BillBankMonthDetailEntity> monthList = billBankMonthDetailDao.findByBillCyclePkId(cyclePkId);
		List<MonthBillDetial> monthBillDetialList = new ArrayList<MonthBillDetial>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		if(CollectionUtils.isNotEmpty(monthList)){
			int monthListSize = monthList.size();
			for(int i = 0; i < monthListSize; i++){
				MonthBillDetial monthBillDetial = new MonthBillDetial();
				BillBankMonthDetailEntity billBankMonthDetailEntity = monthList.get(i);
				monthBillDetial.setIncomeOrPay(billBankMonthDetailEntity.getIncomeOrPay());	// 是否为收入，1收入0支出（默认为0）
				monthBillDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billBankMonthDetailEntity.getCardEndOfFour())); // 卡号末四位
				if(billBankMonthDetailEntity.getMerchandiseDate() != null){
					monthBillDetial.setMerchandiseDate(sdf.format(billBankMonthDetailEntity.getMerchandiseDate()));	// 交易日（明细）
				}else{
					monthBillDetial.setMerchandiseDate("");
				}
				if(billBankMonthDetailEntity.getPostDate() != null){
					monthBillDetial.setPostDate(sdf.format(billBankMonthDetailEntity.getPostDate()));	// 记账日
				}else{
					monthBillDetial.setPostDate("");
				}
				monthBillDetial.setMerchandiseDetail(EmptyUtil.emptyStringHandle(billBankMonthDetailEntity.getMerchandiseDetail())); // 交易摘要
				monthBillDetial.setCurrencyType(EmptyUtil.emptyStringHandle(billBankMonthDetailEntity.getCurrencyType())); // 币种
				monthBillDetial.setAmount(billBankMonthDetailEntity.getAmount()); // 交易金额
				monthBillDetial.setMerchandiseArea(EmptyUtil.emptyStringHandle(billBankMonthDetailEntity.getMerchandiseArea())); // 交易地点
				monthBillDetial.setOriginalTransAmount(billBankMonthDetailEntity.getOriginalTransAmount());	// 交易地金额
				monthBillDetialList.add(monthBillDetial);
			}
		}
		BillCycleInfoEntity billCycleInfoEntity = billCycleInfoDao.findEntityByBillCyclePkId(cyclePkId);
		MonthBill monthBill = new MonthBill();
		monthBill.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP3);
		if(null != billCycleInfoEntity.getBankId()){
			monthBill.setBankId(billCycleInfoEntity.getBankId().toString());
		}
		monthBill.setCardEndOfFour(billCycleInfoEntity.getCardEndOfFour());
		if(null != billCycleInfoEntity.getPaymentDueDate()){
			monthBill.setPaymentDueDate(sdf.format(billCycleInfoEntity.getPaymentDueDate())); // 到期还款日
		}
		monthBill.setAccountOfDate(billCycleInfoEntity.getAccountOfDate());
		monthBill.setBillcycleId(cyclePkId); // 账期主键
//		monthBill.setCardUniqueNo(cardDetial.getCardUniqueNo()); // 卡唯一标示
		monthBill.setMonthbillArr(monthBillDetialList);
		return monthBill;
	}

	@Override
	public DayBill getDayBillsInfo(long cyclePkId, CardDetial cardDetial) throws MailBillException {
		//从月账单详情表查出数据
		List<BillBankDayDetailEntity> dayList = billBankDayDetailDao.findEntityByBillCyclePkId(cyclePkId);
		List<DayBillDetial> dayBillDetialList = new ArrayList<DayBillDetial>();
		String merchandiseDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
		if(CollectionUtils.isNotEmpty(dayList)){
			int dayListSize = dayList.size();
			for(int i = 0; i < dayListSize; i++){
				DayBillDetial dayBillDetial = new DayBillDetial();
				BillBankDayDetailEntity billBankDayDetailEntity = dayList.get(i);
				dayBillDetial.setCardEndOfFour(EmptyUtil.emptyStringHandle(billBankDayDetailEntity.getCardEndOfFour()));		// 卡号末四位
				if(billBankDayDetailEntity.getMerchandiseDate() != null){
					merchandiseDate = sdf.format(billBankDayDetailEntity.getMerchandiseDate());
					dayBillDetial.setMerchandiseDate(merchandiseDate); // 交易日（明细）
				}
				if(billBankDayDetailEntity.getMerchandiseTime() != null){
					dayBillDetial.setMerchandiseTime(sdf2.format(billBankDayDetailEntity.getMerchandiseTime()));// 交易时间
				}
				dayBillDetial.setCurrencyType(EmptyUtil.emptyStringHandle(billBankDayDetailEntity.getCurrencyType())); // 币种
				dayBillDetial.setMerchandiseAmount(EmptyUtil.emptyStringHandle(String.valueOf(billBankDayDetailEntity.getMerchandiseAmount()))); // 交易金额
				dayBillDetial.setMerchandiseDetail(billBankDayDetailEntity.getMerchandiseDetail()); // 商户名称
				dayBillDetial.setIncomeOrPay(billBankDayDetailEntity.getIncomeOrPay());	// 是否为收入，1收入 0支出（默认为0）
				dayBillDetial.setDetail(EmptyUtil.emptyStringHandle(billBankDayDetailEntity.getDetail())); // 信息描述
//				dayBillDetial.setBillCyclePkId(billBankDayDetailEntity.getBillCyclePkId()); // 账单周期表主键
				dayBillDetialList.add(dayBillDetial);
			}
		}
		BillCycleInfoEntity billCycleInfoEntity = billCycleInfoDao.findEntityByBillCyclePkId(cyclePkId);
		DayBill dayBill = new DayBill();
		dayBill.setDataType(MailBillTypeConstants.DATA_TYPE_TO_APP4);
		if (null != billCycleInfoEntity.getBankId()){
			dayBill.setBankId(billCycleInfoEntity.getBankId().toString());
		}
		dayBill.setCardEndOfFour(billCycleInfoEntity.getCardEndOfFour());
		dayBill.setMerchandiseDate(merchandiseDate);
		dayBill.setBillcycleId(cyclePkId); // 账期表主键
//		dayBill.setCardUniqueNo(cardDetial.getCardUniqueNo()); // 卡唯一标示
		dayBill.setDatebillArr(dayBillDetialList);
		return dayBill;
	}

	
	// -------private function -------
	
		/*
		 * 计算要推送的数据包的总量
		 * 
		 * @param mailUrl 邮箱url
		 * 
		 * @param accountId 用户账号
		 * 
		 * @param csVersion 客户端数据版本号
		 * 
		 * @return
		 */
		private int getCountPackage(String mailUrl, Long accountId, String csVersion)
			throws MailBillException {
		int count = 0;

		// 1. 卡数据要推送一个数据包
		Card cardInfo = this.getCardsInfo(mailUrl, accountId, csVersion);
		if (null != cardInfo) {
			++count;
		} else {
			return count;
		}

		// 2. 卡的账期数据(有几张卡就要推几个卡的账期数据)
		if (CollectionUtils.isNotEmpty(cardInfo.getCardArr())) {
			count += cardInfo.getCardArr().size();
		} else {
			return count;
		}

		// 3. 一共有多少账期就有多少数据包(无卡号的除外)
		List<CardDetial> cardArr = cardInfo.getCardArr();
		if (CollectionUtils.isNotEmpty(cardArr)) {
			int length = cardArr.size();
			for (int k = 0; k < length; k++) {
				CardDetial cardDetial = cardArr.get(k);

				String userName = cardDetial.getUserName();
				long bankId = Long.parseLong(cardDetial.getBankId());
				String cardEndOfFour = cardDetial.getCardEndOfFour();
				String paymentDate = cardDetial.getPaymentDueDate();
				
				if (Integer.parseInt(cardDetial.getBillType()) == MailBillTypeConstants.BILL_TYPE_MONTH) {
					Cycle monthCycle = this.geMonthtCylesInfo(mailUrl,
							accountId, csVersion, userName, bankId,
							cardEndOfFour, paymentDate, cardDetial); // 月账单账期数据
					if (null != monthCycle) {
						if (StringUtils.isBlank(cardEndOfFour) && bankId == MailBillTypeConstants.BANK_TYPE_CMB) { // 无卡号的是为新版招商账单,没明细数据
							// 没有明细数据,count不进行递增
						} else { // 有卡号,就有明细数据
							count += monthCycle.getBillcycleArr().size();
						}
					}
				} else {
					Cycle dayCycle = this.getDayCylesInfo(mailUrl, accountId,
							csVersion, userName, bankId, cardEndOfFour, cardDetial); // 日账单账期数据
					if (null != dayCycle) {
						count += dayCycle.getBillcycleArr().size();
					}
				}
			}
		}

		return count;
	}
		
		/*
		 * 当前卡的月账单账期数据及账期下详情数据
		 * 
		 * @param cardDetial 卡数据
		 * 
		 * @param mailUrl 邮箱地址
		 * 
		 * @param accountId 用户账号
		 * 
		 * @param phoneId 手机标示
		 * 
		 * @param csVersion 客户端数据版本号
		 * 
		 * @param socketKey socket长链接唯一标示
		 * 
		 * @param indexPMap 缓存当前已传送的数据包数量
		 * 
		 * @throws MailBillException
		 */
		
		private void pushMonthData(CardDetial cardDetial, String mailUrl,
				Long accountId, String phoneId, String csVersion, String socketKey,
				Map<String, Integer> indexPMap) throws MailBillException {
			StringBuffer sendData = new StringBuffer();
			// 2.1 当前卡的月账单账期数据
			String userName = cardDetial.getUserName();
			long bankId = Long.parseLong(cardDetial.getBankId());
			String cardEndOfFour = cardDetial.getCardEndOfFour();
			String paymentDate = cardDetial.getPaymentDueDate();
			Cycle monthCycle = this.geMonthtCylesInfo(mailUrl, accountId,
					csVersion, userName, bankId, cardEndOfFour, paymentDate, cardDetial); // 月账单账期数据
			int indexPackage = indexPMap.get("indexP").intValue();
			sendData.append("INDEX`");
			sendData.append(++indexPackage);
			this.send(sendData.toString(), socketKey); // 发送正在传送第几个数据包
			String monthCycleJson = "";
			try {
				monthCycleJson = objectMapper.writeValueAsString(monthCycle);
			} catch (Exception ex) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
						ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
			}
			sendData.setLength(0); // 清空缓存
			sendData.append("Y1`"); // 账期
			sendData.append("&" + mailUrl + "&"); // 邮箱地址 TODO 配合测试用例, 测完要删掉
			sendData.append(monthCycleJson);
			this.send(sendData.toString(), socketKey);
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_bill_data:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
			
			// 2.1.1 当前卡的月账期的详情数据
			List<CycleDetial> monthCycleDetials = monthCycle.getBillcycleArr();
			if (CollectionUtils.isNotEmpty(monthCycleDetials)) {
				int monthCDL = monthCycleDetials.size();
				for (int m = 0; m < monthCDL; m++) {
					CycleDetial monthDetail = monthCycleDetials.get(m);
					if (StringUtils.isBlank(cardEndOfFour) && bankId == MailBillTypeConstants.BANK_TYPE_CMB) { // 无卡号,新版招商账单,无明细
						// 不用查询明细
					} else {
						MonthBill monthBill = this.getMonthBillsInfo(monthDetail.getId(), cardDetial); // 月账单明细

						sendData.setLength(0); // 清空缓存
						sendData.append("INDEX`");
						sendData.append(++indexPackage);
						this.send(sendData.toString(), socketKey); // 发送正在传送第几个数据包
						String monthDetailJson = "";
						try {
							monthDetailJson = objectMapper.writeValueAsString(monthBill);
						} catch (Exception ex) {
							throw MailBillExceptionUtil.getWithLog(
									ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
									ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
						}

						sendData.setLength(0); // 清空缓存
						sendData.append("Z1`"); // 详情
						sendData.append("&" + mailUrl + "&"); // 邮箱地址 TODO 配合测试用例, 测完要删掉
						sendData.append(monthDetailJson);
						this.send(sendData.toString(), socketKey);
						log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_detail_data:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
					}
				}
				
				indexPMap.put("indexP", Integer.valueOf(indexPackage));
			}
		}

		/*
		 * 当前卡的日账单账期数据及账期下详情数据
		 * 
		 * @param cardDetial 卡数据
		 * 
		 * @param mailUrl 邮箱地址
		 * 
		 * @param accountId 用户账号
		 * 
		 * @param phoneId 手机标示
		 * 
		 * @param csVersion 客户端数据版本号
		 * 
		 * @param socketKey socket长链接唯一标示
		 * 
		 * @param indexPMap 缓存当前已传送的数据包数量
		 * 
		 * @throws MailBillException
		 */
		
		private void pushDayData(CardDetial cardDetial, String mailUrl,
				Long accountId, String phoneId, String csVersion, String socketKey,
				Map<String, Integer> indexPMap) throws MailBillException {
			StringBuffer sendData = new StringBuffer();
			// 2.2 当前卡的日账单账期数据
			String userName = cardDetial.getUserName();
			long bankId = Long.parseLong(cardDetial.getBankId());
			String cardEndOfFour = cardDetial.getCardEndOfFour();
			Cycle dayCycle = this.getDayCylesInfo(mailUrl, accountId, csVersion,
					userName, bankId, cardEndOfFour, cardDetial); // 日账单账期数据
			int indexPackage = indexPMap.get("indexP").intValue();
			sendData.append("INDEX`");
			sendData.append(++indexPackage);
			this.send(sendData.toString(), socketKey); // 发送正在传送第几个数据包

			String dayCycleJson = "";
			try {
				dayCycleJson = objectMapper.writeValueAsString(dayCycle);
			} catch (Exception ex) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
						ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
			}
			sendData.setLength(0); // 清空缓存
			sendData.append("Y1`"); // 账期
			sendData.append("&" + mailUrl + "&"); // 邮箱地址 TODO 配合测试用例, 测完要删掉
			sendData.append(dayCycleJson);
			this.send(sendData.toString(), socketKey);
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_bill_data:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});

			// 2.2.1 当前卡的月账期的详情数据
			List<CycleDetial> dayCycleDetials = dayCycle.getBillcycleArr();
			if (CollectionUtils.isNotEmpty(dayCycleDetials)) {
				int dayCDL = dayCycleDetials.size();
				for (int d = 0; d < dayCDL; d++) {
					CycleDetial dayDetail = dayCycleDetials.get(d);
					DayBill dayBill = this.getDayBillsInfo(dayDetail.getId(), cardDetial); // 日账单详情

					sendData.setLength(0); // 清空缓存
					sendData.append("INDEX`");
					sendData.append(++indexPackage);
					this.send(sendData.toString(), socketKey); // 发送正在传送第几个数据包

					String dayDetailJson = "";
					try {
						dayDetailJson = objectMapper.writeValueAsString(dayBill);
					} catch (Exception ex) {
						throw MailBillExceptionUtil.getWithLog(
								ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
								ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
					}
					sendData.setLength(0); // 清空缓存
					sendData.append("Z1`"); // 详情
					sendData.append("&" + mailUrl + "&"); // 邮箱地址 TODO 配合测试用例, 测完要删掉
					sendData.append(dayDetailJson);
					this.send(sendData.toString(), socketKey);
					log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tpush_detail_data:{}", new Object[]{mailUrl, accountId, phoneId, sendData.toString()});
				}
				
				indexPMap.put("indexP", Integer.valueOf(indexPackage));
			}
		}
		
}
