package net.umpay.mailbill.restful.rest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.api.model.JspView;
import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;
import net.umpay.mailbill.api.pushapp.IPushMailBill2App;
import net.umpay.mailbill.api.resolve.IBillLogService;
import net.umpay.mailbill.api.resolve.IBillMainViceCardService;
import net.umpay.mailbill.entrance.websocket.MyWebSocket;
import net.umpay.mailbill.hql.model.BillCardInfoEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.hql.model.BillMainViceCardEntity;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.service.SearchMail;
import net.umpay.mailbill.service.impl.resolve.BillCardInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.FindVoServiceImpl;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.security.DesUtil;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 用户邮件账单REST服务
 * @version 1.0.0
 */
@Controller
public class BillUserInfoController extends MyWebSocket {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(BillUserInfoController.class);
	
	@Autowired
	private SearchMail searchMail;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private FindVoServiceImpl findVoService;
	@Autowired
	private IBillLogService billLogService;
	@Autowired
	private IBillMainViceCardService billMainViceCardService;
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillCardInfoServiceImpl billCardInfoServiceImpl;
	@Autowired
	private IPushMailBill2App	pushMailBill2App;
	
	/**
	 * 绑定邮箱 -- [web]
	 * <p>
	 *  <li>账单流程 {保存-->检索邮件-->下载邮件-->解析邮件}</li>
	 * </p>
	 * 
	 * @param emailUrl		邮件地址
	 * @param password		密码
	 * @param phoneId		手机标示
	 * @param accountId		用户账号
	 * @param socketKey		websocket长连接的标示
	 * @return				String
	 * @throws MailBillException
	 */
	
	@SuppressWarnings("static-access")
	@RequestMapping( method = RequestMethod.POST, value = "/webBindingMail.do")
	public String webBindingMail(@RequestParam(value = "emailUrl", required = true) String emailUrl,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "phoneId", required = true) String phoneId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "key", required = true) String socketKey) throws MailBillException {
		log.info(
				"[c->s] action:{} \temailUrl:{} \tpassword:{} \tphoneId:{} \taccountId:{} \tsocketKey:{}",
				new Object[] { ".../webBindingMail.do", emailUrl, password,
						phoneId, accountId, socketKey });
		ModelAndView resultVo = new ModelAndView();
		// 保存邮件
		Map<String, Connection> map = super.connectionMap;
		BillLogEntity billLog = billLogService.getBillLog(phoneId, accountId, emailUrl);
		Map<String, BillLogEntity> logMap = new HashMap<String, BillLogEntity>();
//		long accountId2 = billLog.getAccountId();
		long accountId2 = 123456789; // TODO 测试完事后要注掉-------
		String phoneId2 = billLog.getPhoneId();
		String acc_phone = accountId2 + "_" + phoneId2;
		logMap.put(acc_phone, billLog);
		// 查询账号与邮箱是否已绑定
		boolean ifBinding = billUserInfoServiceImpl.userIfBindingMail(emailUrl, accountId2);
		
		try {
			if (ifBinding){
				// 开始登录搜索
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db exist, search begin", new Object[]{emailUrl, accountId2, phoneId2});
				searchMail.httpOrJavamail(emailUrl, password, socketKey, acc_phone, logMap);
				billUserInfoServiceImpl.updateToBinding(accountId2, emailUrl, password);
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db exist, search end", new Object[]{emailUrl, accountId2, phoneId2});
			}else{
				// 代表此用户未绑定过，先保存过后才登陆
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db no exist, search begin", new Object[]{emailUrl, accountId2, phoneId2});
				searchMail.httpOrJavamail(emailUrl, password, socketKey, acc_phone, logMap);
				billUserInfoServiceImpl.save(emailUrl, password, accountId2);
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db no exist, search end", new Object[]{emailUrl, accountId2, phoneId2});
			}
		} catch (MailBillException e) {
			if (e.getErrorCode() == ErrorCodeContants.POP_NOT_OPEN_EXCEPTION_CODE 
					|| e.getErrorCode() == ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION_CODE){
				this.send("A1`登录失败，您的用户名与密码错误或POP3与IMAP未开启...", socketKey);
				BillLogEntity billLoggerEntity = logMap.get(acc_phone);
				if (null != billLoggerEntity){
	        		billLoggerEntity.setLogOn(MailBillTypeConstants.FAILED);
	        	}
			}
			BillLogEntity billLoggerEntity = logMap.get(acc_phone);
			if (null != billLoggerEntity){
				billLoggerEntity.setLogoutTime(new Date());
				billLogService.save(billLoggerEntity);
			}
			try {
				resultVo.addObject("error", objectMapper.writeValueAsString(e.getErrorCode()));
			} catch (Exception ex) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
						ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
			}
			resultVo.setViewName("failedJsp");
			map.remove(socketKey);
			return "failedJsp";
		}
		// 存储操作日志的推出数据的时间
		BillLogEntity billLoggerEntity = logMap.get(acc_phone);
		if (null != billLoggerEntity){
			billLoggerEntity.setLogoutTime(new Date());
			billLogService.save(billLoggerEntity);
		}
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} redirect:/findCycleInfo.do", new Object[]{emailUrl, accountId2, phoneId2});
		
		map.remove(socketKey);
		return "redirect:/findCycleInfo.do?emailUrl="+emailUrl;
	}
	
	/**
	 * 账单周期列表信息-- [web]
	 * 
	 * @param emailUrl		邮箱url
	 * @return ModelAndView
	 * @throws MailBillException 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/findCycleInfo.do")
	public ModelAndView findCycleInfo(@RequestParam(value = "emailUrl", required = true) String emailUrl) 
			throws MailBillException {
		log.info("[c->s] action:{} \temailUrl:{} begin", new Object[]{".../findCycleInfo.do", emailUrl });
		ModelAndView resultVo = new ModelAndView();
		JspView jsp = new JspView();
		List<JspInfoPartView> view = new ArrayList<JspInfoPartView>(); // 接收账单
		List<JspInfoPartView> notview = new ArrayList<JspInfoPartView>();
		log.info("e_mail:{} query cycleinfo by emailUrl begin", emailUrl);
		List<JspInfoPartView> infoViews = billCycleInfoServiceImpl.findEntityByEmailUrl(emailUrl);
		
		for (JspInfoPartView infoView : infoViews){
			if (1 == infoView.getIsBill()){
				view.add(infoView);
			}else{
				notview.add(infoView);
			}
		}
		jsp.setView(view);
		jsp.setNotview(notview);
		log.info("e_mail:{} query cycleinfo by emailUrl end", emailUrl);
		try {
			resultVo.addObject("jsp", objectMapper.writeValueAsString(jsp));
		} catch (Exception ex) {
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
		}
		resultVo.setViewName("content/manage/billUserInfo-list");
		log.info("e_mail:{} return page viewName:{}", new Object[]{emailUrl, "content/manage/billUserInfo-list"});
		log.info("[c->s] action:{} \temailUrl:{} end", new Object[]{".../findCycleInfo.do", emailUrl });
		return resultVo;
	}
	
	/**
	 * 账单详情-- [web]
	 * 
	 * @param billCyclePkId		周期表主键
	 * @param voBillType		账单详细类型
	 * @return ModelAndView
	 * @throws MailBillException 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/findBill.do")
	public ModelAndView findBill(@RequestParam(value = "billCyclePkId", required = true) Long billCyclePkId,
			@RequestParam(value = "voBillType", required = true) int voBillType) 
			throws MailBillException{
		ModelAndView resultVo = new ModelAndView();
		log.info("[c->s] action:{} \tbillType:{} \tbillCyclePkId:{} begin", new Object[]{".../findBill.do", voBillType, billCyclePkId});
		BillTypeView view = findVoService.getView(voBillType, billCyclePkId);
		int billType = view.getBillType();
		log.info("[c->s] action:{} \tbillType:{} \tbillCyclePkId:{} end", new Object[]{".../findBill.do", voBillType, billCyclePkId});
		try {
			resultVo.addObject("view", objectMapper.writeValueAsString(view));
		} catch (Exception ex) {
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.JSON_OBJECT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_OBJECT_EXCEPTION.getMsg(), log);
		}
		resultVo.setViewName("content/manage/"+(billType == 1 ? "billCommonMonth" : "billCommonDay"));
		log.info("return page viewName:{}", "content/manage/billUserInfo/"+(billType == 0 ? "billCommonMonth" : "billCommonDay"));
		
		return resultVo;
	}
	
	
	/**
	 * 全量刷新或单个刷新邮箱 -- [App]
	 * 
	 * @param mailArr	邮箱数组
	 * @param phoneId	手机标示
	 * @param accountId	用户账号
	 * @param csVersion	客户端数据版本号
	 * @param socketKey socket长连接的唯一标示
	 * @throws MailBillException
	 */
	@SuppressWarnings("static-access")
	@RequestMapping(method = RequestMethod.POST, value = "/refreshMails.do")
	public String refreshMails(
			@RequestParam(value = "mailArr", required = true) String[] mailArr,
			@RequestParam(value = "phoneId", required = true) String phoneId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "csVersion", required = true) String csVersion,
			@RequestParam(value = "socketKey", required = true) String socketKey)
			throws MailBillException {
		
		Map<String, Connection> socketConnectionMap = super.connectionMap;
		
		this.send("BEGIN`SEND BEGIN", socketKey);
		for (int i = 0; i < mailArr.length; i++) {
			String emailUrl = mailArr[i];
			String password = billUserInfoServiceImpl.findPasswordByMailUrl(emailUrl);
			password = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(),password);
			// logger
			BillLogEntity billLog = billLogService.getBillLog(phoneId, accountId, emailUrl);
			Map<String, BillLogEntity> loggerMap = new HashMap<String, BillLogEntity>();
			long logAccountId = billLog.getAccountId();
			String logPhoneId = billLog.getPhoneId();
			String acc_phone = logAccountId + "_" + logPhoneId;
			loggerMap.put(acc_phone, billLog);
			// 执行抓取操作
			searchMail.httpOrJavamail(emailUrl, password, socketKey, acc_phone, loggerMap);
			// 调用推送客户端接口 TODO 该方法得到所有邮箱数据的包数目
			pushMailBill2App.pushMailBillEntrance(emailUrl, logAccountId, logPhoneId, csVersion, socketKey);
			
		}
		this.send("END`SEND END", socketKey);
		socketConnectionMap.remove(socketKey); // 回收链接
		
		return "";
	}
	
	/**
	 * 邮箱解绑  -- [App]
	 * @param emailUrl	邮件地址
	 * @param phoneId	手机标示
	 * @param accountId	用户账号
	 * @param socketKey	socket长连接的唯一标示
	 * @throws MailBillException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/unBindingMail.do")
	public String unBindingMail(
			@RequestParam(value = "emailUrl", required = true) String emailUrl,
			@RequestParam(value = "phoneId", required = true) String phoneId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "socketKey", required = true) String socketKey)
		throws MailBillException {
		log.info(
				"[c->s] action:{} \temailUrl:{} \tphoneId:{} \taccountId:{} \tsocketKey:{}",
				new Object[] { ".../unBindingMail.do", emailUrl, phoneId,
						accountId, socketKey });
		// 调用推送客户端接口
		pushMailBill2App.pushUnBindingMail(emailUrl, accountId, phoneId, socketKey);
		
		return "";
	}
	
	/**
	 * 获取用户邮箱列表 -- [App]
	 * 
	 * @param emailUrl	邮件地址
	 * @param phoneId	手机标示
	 * @param accountId	用户账号
	 * @param socketKey	socket长连接的唯一标示
	 * @throws MailBillException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/accountBindingMails.do")
	public String accountBindingMails(
			@RequestParam(value = "emailUrl", required = true) String emailUrl,
			@RequestParam(value = "phoneId", required = true) String phoneId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "socketKey", required = true) String socketKey)
			throws MailBillException {
		log.info(
				"[c->s] action:{} \temailUrl:{} \tphoneId:{} \taccountId:{} \tsocketKey:{}",
				new Object[] { ".../accountBindingMails.do", emailUrl, phoneId,
						accountId, socketKey });
		// 调用推送客户端接口
		this.send("BEGIN`SEND BEGIN", socketKey);
		pushMailBill2App.pushAccountOfMails(emailUrl, accountId, phoneId, socketKey);
		this.send("END`SEND END", socketKey);
		
		return "";
	}
	
	/**
	 * 绑定邮箱 -- [App]
	 * <p>
	 *  <li>绑定流程： </li>
	 *  <li>1.账号绑定邮箱;2.检索邮件;3.下载邮件;4.解析邮件;</li>
	 * </p>
	 * 
	 * @param emailUrl		邮件地址
	 * @param password		密码
	 * @param phoneId		手机标示
	 * @param accountId		用户账号
	 * @param csVersion		客户端数据版本号
	 * @param socketKey		socket长连接的唯一标示
	 * @throws MailBillException
	 */
	@SuppressWarnings("static-access")
	@RequestMapping(method = RequestMethod.POST, value = "/bindingMail.do")
	public String appBindingMail(
			@RequestParam(value = "emailUrl", required = true) String emailUrl,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "phoneId", required = true) String phoneId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "csVersion", required = true) String csVersion,
			@RequestParam(value = "socketKey", required = true) String socketKey)
			throws MailBillException {
		log.info(
				"[c->s] action:{} \temailUrl:{} \tpassword:{} \tphoneId:{} \taccountId:{} \tcsVersion:{} \tsocketKey:{} begin",
				new Object[] { ".../bindingMail.do", emailUrl, password,
						phoneId, accountId, csVersion, socketKey });
		Map<String, Connection> socketConnectionMap = super.connectionMap;
		// logger
		BillLogEntity billLog = billLogService.getBillLog(phoneId, accountId, emailUrl);
		Map<String, BillLogEntity> loggerMap = new HashMap<String, BillLogEntity>();
		long logAccountId = billLog.getAccountId();
		String logPhoneId = billLog.getPhoneId();
		String acc_phone = logAccountId + "_" + logPhoneId;
		loggerMap.put(acc_phone, billLog);
		// 查询账号与邮箱是否已绑定
		boolean ifBinding = billUserInfoServiceImpl.userIfBindingMail(emailUrl, logAccountId);
		
		try {
			if (ifBinding){
				// 开始登录搜索
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db exist, search begin", new Object[]{emailUrl, logAccountId, logPhoneId});
				searchMail.httpOrJavamail(emailUrl, password, socketKey, acc_phone, loggerMap);
				billUserInfoServiceImpl.updateToBinding(logAccountId, emailUrl, password);
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db exist, search end", new Object[]{emailUrl, logAccountId, logPhoneId});
			}else{
				// 代表此用户未绑定过，先保存过后才登陆
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db no exist, search begin", new Object[]{emailUrl, logAccountId, logPhoneId});
				searchMail.httpOrJavamail(emailUrl, password, socketKey, acc_phone, loggerMap);
				// 绑定邮箱
				billUserInfoServiceImpl.save(emailUrl, password, logAccountId);
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} user mail db no exist, search end", new Object[]{emailUrl, logAccountId, logPhoneId});
			}
		} catch (MailBillException e) {
			if (e.getErrorCode() == ErrorCodeContants.POP_NOT_OPEN_EXCEPTION_CODE 
					|| e.getErrorCode() == ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION_CODE){
				this.send("ERR1`-205&登录失败，您的用户名与密码错误或POP3与IMAP未开启", socketKey);
				BillLogEntity billLoggerEntity = loggerMap.get(acc_phone);
				if (null != billLoggerEntity){
	        		billLoggerEntity.setLogOn(MailBillTypeConstants.FAILED);
	        	}
			}
			BillLogEntity billLoggerEntity = loggerMap.get(acc_phone);
			if (null != billLoggerEntity){
				billLoggerEntity.setLogoutTime(new Date());
				billLogService.save(billLoggerEntity);
			}
			socketConnectionMap.remove(socketKey);
		}
		// 存储操作日志的推出数据的时间
		BillLogEntity billLoggerEntity = loggerMap.get(acc_phone);
		if (null != billLoggerEntity){
			billLoggerEntity.setLogoutTime(new Date());
			billLogService.save(billLoggerEntity);
		}
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} redirect:/findCycleInfo.do", new Object[]{emailUrl, logAccountId, logPhoneId});
	
		// 调用推送客户端接口
		this.send("BEGIN`SEND BEGIN", socketKey);
		pushMailBill2App.pushMailBillEntrance(emailUrl, logAccountId, logPhoneId, csVersion, socketKey);
		this.send("END`SEND END", socketKey);
		socketConnectionMap.remove(socketKey); // 回收链接         TODO 最后要打开,本次修改是为了测试用例配置多个邮箱---------------------------------------
		log.info(
				"[c->s] action:{} \temailUrl:{} \tpassword:{} \tphoneId:{} \taccountId:{} \tcsVersion:{} \tsocketKey:{} end",
				new Object[] { ".../bindingMail.do", emailUrl, password,
						phoneId, accountId, csVersion, socketKey });
		return "";
	}
	
	/**
	 * 设置主副卡关系
	 * 
	 * @param bankId			银行标示id
	 * @param infoSourceEmail	账单来源
	 * @param accountId			用户账号
	 * @param oldMainCardOfFour	原主卡卡号末四位
	 * @param newMainCardOfFour	新主卡卡号末四位
	 * @param viceCardOfFour	副卡卡号末四位串 （多个副卡，以“,”分割）
	 * @throws MailBillException
	 */
    @Deprecated
	//@RequestMapping(method = RequestMethod.PUT, value = "/{bankId:\\d+}/{infoSourceEmail}/{accountId:\\d+}/mainViceCard.do")
	public void mainViceCardSet(
			@PathVariable("bankId") Integer bankId,
			@PathVariable("infoSourceEmail") String infoSourceEmail,
			@PathVariable("accountId") long accountId,
			@RequestParam(value = "oldMainCardOfFour", required = true) String oldMainCardOfFour,
			@RequestParam(value = "newMainCardOfFour", required = true) String newMainCardOfFour,
			@RequestParam(value = "viceCardOfFour", required = true) String viceCardOfFour)
			throws MailBillException {
		// 查询关系表中是否存在相关信息
		List<BillMainViceCardEntity> mailViceCardList = billMainViceCardService
				.find(bankId, accountId, infoSourceEmail, oldMainCardOfFour);
		// 若存在即更新
		if (CollectionUtils.isNotEmpty(mailViceCardList)) {
			BillMainViceCardEntity billMainViceCardEntity = mailViceCardList.get(0);
			billMainViceCardEntity.setMainCardOfFour(newMainCardOfFour);
			billMainViceCardEntity.setViceCardOfFour(viceCardOfFour);
			billMainViceCardService.update(billMainViceCardEntity);
		} else { // 不存在即插入
			billMainViceCardService.save(bankId, accountId, infoSourceEmail,
					newMainCardOfFour, viceCardOfFour);
		}
	}
	
	/**
	 * 设置账单日
	 * 
	 * @param bankId			银行标示id
	 * @param accountId			用户账号
	 * @param infoSourceEmail	账单来源
	 * @param mainCardOfFour	主卡卡号后四位
	 * @param billDate			账单日
	 */
    @Deprecated
//	@RequestMapping(method = RequestMethod.PUT, value = "/setBillDate.do")
	public void setBillDate (
			@RequestParam(value = "bankId", required = true) Integer bankId, 
			@RequestParam(value = "accountId", required = true) Long accountId, 
			@RequestParam(value = "infoSourceEmail", required = true) String infoSourceEmail, 
			@RequestParam(value = "mainCardOfFour", required = true) String mainCardOfFour, 
			@RequestParam(value = "billDate", required = true) Integer billDate) throws MailBillException{
		//根据指定的bankId、accountId、mainCardOfFour从卡信息表中查出数据
		List<BillCardInfoEntity> listInfo = billCardInfoServiceImpl.find(bankId, accountId, mainCardOfFour);
		//不为空则直接设置账单日
		if(!CollectionUtils.isNotEmpty(listInfo)){
			billCardInfoServiceImpl.setBillDate(bankId, accountId, infoSourceEmail, mainCardOfFour, billDate);
		}
		//为空则到账期表查询
		else{
			List<BillCycleInfoEntity> cycleInfolist = billCycleInfoServiceImpl.findInfoByBIC(bankId, infoSourceEmail, mainCardOfFour);
			if(!CollectionUtils.isNotEmpty(cycleInfolist)){
				BillCardInfoEntity entity = new BillCardInfoEntity();
				entity.setBankId(bankId);
				entity.setAccountId(accountId);
				entity.setInfoSourceEmail(infoSourceEmail);
				entity.setMainCardOfFour(mainCardOfFour);
				entity.setBillDate(billDate);
				//以下数据从账期表中取得
				entity.setPaymentDueDate(cycleInfolist.get(0).getPaymentDueDate());	//还款日
				entity.setRmbCreditLimit(cycleInfolist.get(0).getRmbCreditLimit());	//人民币信用额度
				entity.setUsaCreditLimit(cycleInfolist.get(0).getUsaCreditLimit());	//美元信用的度
				entity.setCardType(cycleInfolist.get(0).getCardType());				//银行卡卡种
				entity.setUserName(cycleInfolist.get(0).getUserName());				//用户名
				entity.setUserGender(cycleInfolist.get(0).getUserGender());			//用户性别尊称（先生、女士）
				entity.setCashUsaAdvanceLimit(cycleInfolist.get(0).getCashUsaAdvanceLimit());	//美元预借现金额度
				entity.setCashRmbAdvanceLimit(cycleInfolist.get(0).getCashRmbAdvanceLimit());	//人民币预借现金额度
				billCardInfoServiceImpl.save(entity);
			}
		}
	}
	
	/**
	 * 设置还款日
	 * 
	 * @param bankId			银行标示id
	 * @param accountId			用户账号
	 * @param infoSourceEmail	账单来源
	 * @param mainCardOfFour	主卡卡号后四位
	 * @param paymentDueDate	还款日
	 */
    @Deprecated
//	S@RequestMapping(method = RequestMethod.PUT, value = "/setPaymentDueDate.do")
	public void setPaymentDueDate(
			@RequestParam(value = "bankId", required = true) Integer bankId, 
			@RequestParam(value = "accountId", required = true) Long accountId, 
			@RequestParam(value = "infoSourceEmail", required = true) String infoSourceEmail, 
			@RequestParam(value = "mainCardOfFour", required = true) String mainCardOfFour, 
			@RequestParam(value = "paymentDueDate", required = true) Date paymentDueDate) throws MailBillException{
		//根据指定的bankId、accountId、mainCardOfFour从卡信息表中查出数据
		List<BillCardInfoEntity> listInfo = billCardInfoServiceImpl.find(bankId, accountId, mainCardOfFour);
		//不为空则直接设置账单日
		if(!CollectionUtils.isNotEmpty(listInfo)){
			billCardInfoServiceImpl.setPaymentDueDate(bankId, accountId, infoSourceEmail, mainCardOfFour, paymentDueDate);
		}
		//为空则到账期表查询
		else{
			List<BillCycleInfoEntity> cycleInfolist = billCycleInfoServiceImpl.findInfoByBIC(bankId, infoSourceEmail, mainCardOfFour);
			if(!CollectionUtils.isNotEmpty(cycleInfolist)){
				BillCardInfoEntity entity = new BillCardInfoEntity();
				entity.setBankId(bankId);
				entity.setAccountId(accountId);
				entity.setInfoSourceEmail(infoSourceEmail);
				entity.setMainCardOfFour(mainCardOfFour);
				entity.setPaymentDueDate(paymentDueDate);
				//以下数据从账期表中取得
				entity.setBillDate(cycleInfolist.get(0).getBillDate());				//账单日
				entity.setRmbCreditLimit(cycleInfolist.get(0).getRmbCreditLimit());	//人民币信用额度
				entity.setUsaCreditLimit(cycleInfolist.get(0).getUsaCreditLimit());	//美元信用的度
				entity.setCardType(cycleInfolist.get(0).getCardType());				//银行卡卡种
				entity.setUserName(cycleInfolist.get(0).getUserName());				//用户名
				entity.setUserGender(cycleInfolist.get(0).getUserGender());			//用户性别尊称（先生、女士）
				entity.setCashUsaAdvanceLimit(cycleInfolist.get(0).getCashUsaAdvanceLimit());	//美元预借现金额度
				entity.setCashRmbAdvanceLimit(cycleInfolist.get(0).getCashRmbAdvanceLimit());	//人民币预借现金额度
				billCardInfoServiceImpl.save(entity);
			}
		}
	}
	
	/**
	 * 设置卡品牌
	 * 
	 * @param bankId			银行标示id
	 * @param accountId			用户账号
	 * @param infoSourceEmail	账单来源
	 * @param mainCardOfFour	主卡卡号后四位
	 * @param cardType			卡品牌
	 */
    @Deprecated
//	@RequestMapping(method = RequestMethod.PUT, value = "/setCardType.do")
	public void setCardType(
			@RequestParam(value = "bankId", required = true) Integer bankId, 
			@RequestParam(value = "accountId", required = true) Long accountId, 
			@RequestParam(value = "infoSourceEmail", required = true) String infoSourceEmail, 
			@RequestParam(value = "mainCardOfFour", required = true) String mainCardOfFour, 
			@RequestParam(value = "paymentDueDate", required = true) String cardType) throws MailBillException{
		//根据指定的bankId、accountId、mainCardOfFour从卡信息表中查出数据
		List<BillCardInfoEntity> listInfo = billCardInfoServiceImpl.find(bankId, accountId, mainCardOfFour);
		//不为空则直接设置账单日
		if(!CollectionUtils.isNotEmpty(listInfo)){
			billCardInfoServiceImpl.setCardType(bankId, accountId, infoSourceEmail, mainCardOfFour, cardType);
		}
		//为空则到账期表查询
		else{
			List<BillCycleInfoEntity> cycleInfolist = billCycleInfoServiceImpl.findInfoByBIC(bankId, infoSourceEmail, mainCardOfFour);
			if(!CollectionUtils.isNotEmpty(cycleInfolist)){
				BillCardInfoEntity entity = new BillCardInfoEntity();
				entity.setBankId(bankId);
				entity.setAccountId(accountId);
				entity.setInfoSourceEmail(infoSourceEmail);
				entity.setMainCardOfFour(mainCardOfFour);
				entity.setCardType(cardType);
				//以下数据从账期表中取得
				entity.setPaymentDueDate(cycleInfolist.get(0).getPaymentDueDate());	//还款日
				entity.setBillDate(cycleInfolist.get(0).getBillDate());				//账单日
				entity.setRmbCreditLimit(cycleInfolist.get(0).getRmbCreditLimit());	//人民币信用额度
				entity.setUsaCreditLimit(cycleInfolist.get(0).getUsaCreditLimit());	//美元信用的度
				entity.setUserName(cycleInfolist.get(0).getUserName());				//用户名
				entity.setUserGender(cycleInfolist.get(0).getUserGender());			//用户性别尊称（先生、女士）
				entity.setCashUsaAdvanceLimit(cycleInfolist.get(0).getCashUsaAdvanceLimit());	//美元预借现金额度
				entity.setCashRmbAdvanceLimit(cycleInfolist.get(0).getCashRmbAdvanceLimit());	//人民币预借现金额度
				billCardInfoServiceImpl.save(entity);
			}
		}
	}
	
	/**
	 * 卡刷新登录 
	 * 
	 * @param	bankId				银行类型
	 * @param	accountId			用户账号
	 * @param	mainCardOfFour		主卡卡号
	 * @param	key					key值
	 * @param	accountId_phoneId
	 * @throws	MailBillException
	 */
    @Deprecated
//	@RequestMapping(method = RequestMethod.GET, value = "/refreshCard.do")
	public void refreshCard(
			@RequestParam(value = "bankId", required = true) Integer bankId,
			@RequestParam(value = "accountId", required = true) Long accountId,
			@RequestParam(value = "mainCardOfFour", required = true) String mainCardOfFour,
			@RequestParam(value = "key", required = true) String key,
			@RequestParam(value = "accountId_phoneId", required = true) String accountId_phoneId) throws MailBillException{
		//根据指定的bankId、accountId、mainCardOfFour从卡信息表中查出用户的邮箱地址
		List<BillCardInfoEntity> listInfo = billCardInfoServiceImpl.find(bankId, accountId, mainCardOfFour);
		String mailUrl = listInfo.get(0).getInfoSourceEmail();
		String password = null;
		Map<String, BillLogEntity> logMap = new HashMap<String, BillLogEntity>();
		//根据查出的邮箱地址到userInfo表中查出password 完成登录
		if(!CollectionUtils.isNotEmpty(listInfo)){
			List<BillUserInfoEntity> userInfoList = billUserInfoServiceImpl.findByMailUrl(mailUrl);
			if(!CollectionUtils.isNotEmpty(userInfoList)){
				password = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(), userInfoList.get(0).getPassword());
				searchMail.httpOrJavamail(mailUrl, password, key, accountId_phoneId, logMap);
			}
		}
		//卡信息表中未查出数据 则到账期表去查用户的邮箱地址
		else{
			List<BillCycleInfoEntity> cycleInfoList = billCycleInfoServiceImpl.findInfoByBAC(bankId, accountId, mainCardOfFour);
			if(!CollectionUtils.isNotEmpty(cycleInfoList)){
				mailUrl = cycleInfoList.get(0).getInfoSource();
				List<BillUserInfoEntity> userInfoList = billUserInfoServiceImpl.findByMailUrl(mailUrl);
				password = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(), userInfoList.get(0).getPassword());
				searchMail.httpOrJavamail(mailUrl, password, key, accountId_phoneId, logMap);
			}
		}
	}
	
}
