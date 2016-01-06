package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import net.umpay.mailbill.api.banktemplate.IBankTemplateService;
import net.umpay.mailbill.api.mailhandle.ImailJudge;
import net.umpay.mailbill.entrance.websocket.MyWebSocket;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;
import net.umpay.mailbill.hql.model.ThreadRunAbleTest;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.NotBillCycleInfoServiceImpl;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.date.DateUtil;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.mail.BankBillTypesFactory;
import net.umpay.mailbill.util.mail.MailAnalyze;
import net.umpay.mailbill.util.string.ReadProperty;
import net.umpay.mailbill.util.string.TextExtract;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 邮件 -- 解析、数据格式化存储
 * @version 1.0.0
 */
@Service
public class ParseMailService extends MyWebSocket{
	private static Logger log = LoggerFactory.getLogger(ParseMailService.class);
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private NotBillCycleInfoServiceImpl notBillCycleInfoServiceImpl;
	@Autowired
	private MailAnalyze mailAnalyze;
	@Autowired
	private ImailJudge imailJudge;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor ;
	
	// 模式分解各类功能
	private List<IBankTemplateService> bankTemplateServicesList;
	public List<IBankTemplateService> getBankTemplateServicesList() {
		return bankTemplateServicesList;
	}
	public void setBankTemplateServicesList(
			List<IBankTemplateService> bankTemplateServicesList) {
		this.bankTemplateServicesList = bankTemplateServicesList;
	}
	
	/**
	 * [pop方式]邮件解析、数据格式化存储
	 * 
	 * @param user_mail				用户邮箱
	 * @param key					websoket长连接标示
	 * @param accountid_phoneId		操作日志信息记录的唯一标示
	 * @param logMap				缓存日志信息
	 * @param scVersion				服务端版本号
	 * @param messages				要解析的邮件列表
	 * @throws MailBillException
	 */
	
	public void parseMessage(String user_mail, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap, Long scVersion, Message... messages) 
			throws MailBillException {
		if (messages == null || messages.length < 1){
			log.error("e_mail:{} messages not found will be parsing", user_mail);
			this.send("A1`未发现将要解析的邮件", key);
			throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.NOT_FOUND_PARSED_MAIL_CODE, ErrorCodeContants.NOT_FOUND_PARSED_MAIL.getMsg(), log);
		}
		
		Map<String, String> map = billCycleInfoServiceImpl.findExistEmailCount(user_mail);
		List<MimeMessage> newlist = new ArrayList<MimeMessage>();
		int count = messages.length;
		for (int i = 0; i < count; i++) {
			MimeMessage msg = (MimeMessage) messages[i];
			int messageNumber = msg.getMessageNumber();
			log.info("e_mail:{} querying:{} e-mail",new Object[]{user_mail, messageNumber});
			this.send("A1`正在查询第：" + (i+1) + "/" + count +"封邮件", key);
			String subject = mailAnalyze.getSubject(msg);
			String receiveAdd = mailAnalyze.getReceiveAddress(msg, null);
			String senderAdd = mailAnalyze.getFrom(msg);
			String sentData = mailAnalyze.getSentDate(msg, null);
			String[] path = mailAnalyze.getPath(subject, receiveAdd, sentData, senderAdd, user_mail);
			// 存储疑似账单
			if(StringUtils.isBlank(map.get(path[0]))){ // 库中无此记录,是为新发现的疑似账单
				newlist.add(msg);
			}
		}
		int size = newlist.size();
		if (0 != size){
			log.info("e_mail:{} \tfound_new_suspected_bill_count:{}", new Object[]{user_mail, size});
			this.send("A1`发现有："+ size +"封新疑似账单邮件", key);
			CountDownLatch downLatch = new CountDownLatch(size);
			// 解析所有邮件
			for (int i = 0; i < size; i++) {
				MimeMessage msg = newlist.get(i);
				int messageNumber = msg.getMessageNumber();
				log.info("e_mail:{} \tstart_processing_bill_number:{}", new Object[]{user_mail, messageNumber});
				this.send("B1`正在解析第：" + (i+1) + "/" + size +"封邮件", key);
				String subject = mailAnalyze.getSubject(msg);
				String receiveAdd = mailAnalyze.getReceiveAddress(msg, null);
				String senderAdd = mailAnalyze.getFrom(msg);
				String sentData = mailAnalyze.getSentDate(msg, null);
				log.info("e_mail:{} \tsubject:{} \tthe_sender:{} \taddressee:{} \tsend_time{}", new Object[]{user_mail, subject, senderAdd, receiveAdd, sentData});
				StringBuffer content = new StringBuffer(100);
				mailAnalyze.getMailTextContent(msg, content);
				//线程启动
				taskExecutor.execute(new ThreadRunAbleTest(this, downLatch, subject, receiveAdd, content, sentData, senderAdd, user_mail, messageNumber, key, accountid_phoneId, logMap, size, scVersion));
			}
			try {
				downLatch.await(); // 计数不为零，继续等待
			} catch (InterruptedException e) {
				MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INTERRUPTED_EXCEPTION_CODE, ErrorCodeContants.INTERRUPTED_EXCEPTION.getMsg(), log);
			}
		}else{
			log.info("e_mail:{} not found new mail bill", user_mail);
			this.send("A1`没发现新的疑似账单邮件", key);
		}
	}
	
	/**
	 * 将数据进行下载==》解析==》入库操作
	 * 
	 * @param subject				邮件主题
	 * @param receiveAdd			收件人邮件地址
	 * @param content				邮件主要内容
	 * @param sentData				发件时间
	 * @param senderAdd				发件人邮件地址
	 * @param user_mail				用户登录邮箱地址
	 * @param messageNumber			邮箱内第几封邮件序号
	 * @param key					websocket长连接唯一标示	
	 * @param accountid_phoneId		操作日志信息标志
	 * @param logMap				操作日志的map
	 * @param scVersion				服务端版本号
	 * @throws MailBillException
	 */
	public void downloadMail(String subject, String receiveAdd, StringBuffer content, String sentData, String senderAdd,
			String user_mail, int messageNumber, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap, Long scVersion)
			throws MailBillException {
		// 将过滤到的邮件下载
		BankBillTypesFactory bankBillTypesFactory = new BankBillTypesFactory(); // 简单工厂 
		String[] path1 = new String[2]; // 第一次下载后地址
		String[] path2 = new String[2]; // 第二次截取后地址
		
		BillLogEntity billLoggerEntity = logMap.get(accountid_phoneId);
		int downloadNumber = 0;
		int analysisNumber = 0;
		int exceptionNumber = 0;
		
		//分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		Long accountId2 = null;
		if (split.length != 0){
			accountId = split[0];
			accountId2 = Long.valueOf(accountId);
			phoneId = split[1];
		}
		// 将过滤到的邮件下载
		String[] fpath = mailAnalyze.getPath(subject, receiveAdd, sentData, senderAdd, user_mail );
		String sb = mailAnalyze.downLoadLinux(content, fpath[3],  fpath[0], fpath[1], fpath[2]);
		if (StringUtils.isBlank(sb)){
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.DFS_GETCONTENT_FAILED_CODE, ErrorCodeContants.DFS_GETCONTENT_FAILED.getMsg(), log);
		}else{
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \treceiveAdd:{} \tsentData:{} \tsenderAdd:{} \tsubject:{} uploadFile", 
					new Object[]{user_mail, accountId, phoneId, receiveAdd, sentData, senderAdd, subject});
			
			if (billLoggerEntity != null){
				downloadNumber = billLoggerEntity.getDownloadNumber() + 1;
				billLoggerEntity.setDownloadNumber(downloadNumber);
			}
			path1[0] = fpath[0];
			path2[0] = fpath[1];
			//分割内容与地址
			int indexOf = sb.indexOf(";;;");
			int index = sb.indexOf("= =");
			path1[1] = sb.substring(0, index);
			path2[1] = sb.substring(index+3, indexOf);
			
			sb = sb.substring(indexOf+3);
			List<String> parse ;
			// 格式化文本内容
			Document document = Jsoup.parse(sb);
			// 从文本中截取内容
			if (subject.contains("中国银行")){
				new ChinaBank().replaceTdOfNull(document);//此处将单元格内容为空的替换为空"&nbsp;"以此来方便区分收入与支出
				parse = new TextExtract().parse(document.toString());
			}else{
				parse = new TextExtract().parse(document.toString());
			}
			//TODO  打印
			/*for(int i = 0;i < parse.size(); i++){
				System.out.println(i+"===="+parse.get(i));
			}*/
			// 判断邮件是否为账单邮件
			boolean is_mailBill = imailJudge.Judge(parse, subject);
			if(is_mailBill == true){
				// 工厂模式返回一个银行实例
				int bank_type = bankBillTypesFactory.getMailBillTypes(subject);
				  if (-1 != bank_type) { // 如果没有返回银行实例，可能是新版的银行或者账单
					  for (IBankTemplateService service : bankTemplateServicesList) {
						  if (bank_type == service.getBankType()) { // 首先预先知道银行类型
							 try{
								 this.send("G1`"+bank_type+",主题:"+subject, key);
								 String bankTemplateParse = service.bankTemplateParse(parse, ReadProperty.getEmailUrl(fpath[0],2), path1, path2, new Long[0], accountId2, scVersion);
								 if (bankTemplateParse.equals("00")){
									 log.error("e_mail:{} \taccount_id:{} \tphone_id:{} \tsubject:{} \tnew_type_bill", new Object[]{user_mail, accountId, phoneId, subject});
									 saveBillCycle(path1, path2, "请注意,可能是新类型账单..."+path1.toString());
									 MailBillExceptionUtil.getWithLog(ErrorCodeContants.NO_THIS_BILLING_TYPE_TEMPLATE_CODE, ErrorCodeContants.NO_THIS_BILLING_TYPE_TEMPLATE.getMsg(), log);
								 }
								 if (billLoggerEntity != null){
									 analysisNumber = billLoggerEntity.getAnalysisNumber() + 1;
									 billLoggerEntity.setAnalysisNumber(analysisNumber);
								 }
								 break;
							 } catch (Exception e) {
								 e.printStackTrace();
								 log.error("exception info:{}:", new Object[]{e.getMessage()});
								 log.error("e_mail:{} \taccount_id:{} \tphone_id:{} \tsubject:{}", new Object[]{user_mail, accountId, phoneId, subject});
								 if (billLoggerEntity != null){
									exceptionNumber = billLoggerEntity.getExceptionNumber() + 1;
									billLoggerEntity.setExceptionNumber(exceptionNumber);
								 }
								 if(StringUtils.isBlank(e.getMessage())){
									 saveBillCycle(path1, path2, "其主题为："+subject+"银行解析账单有误!");
									 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.NULL_CODE, e.getMessage(), log);
								 }else if (e.getMessage().contains("Index:") || e.getMessage().contains("String index out of range:")){
									 saveBillCycle(path1, path2, "其主题为："+subject+"银行解析账单有误!");
									 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION_CODE, e.getMessage(), log);
								 }else if (e.getMessage().contains("转换日期发生错误")){
									 saveBillCycle(path1, path2, "其主题为："+subject+"银行解析账单有误!");
									 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DATA_TYPE_CONVERSION_CODE, e.getMessage(), log);
								 }else if(e.getMessage().contains("Could not execute JDBC batch update")){
									 saveBillCycle(path1, path2, "其主题为："+subject+"银行解析账单有误!");
									 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
								 }else{
									 saveBillCycle(path1, path2, "其主题为："+subject+"银行解析账单有误!");
									 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
								 }
							 }
						  }
					 }
				  }else{
					  log.error("e_mail:{} \taccount_id:{} \tphone_id:{} \tunknown_bank_bill_subject_is:{}", new Object[]{user_mail, accountId, phoneId, subject});
					  saveBillCycle(path1, path2, "未知银行账单,其主题是:"+subject);
				  }
			} else {
				saveBillCycle(path1, path2, null);
			}
		}
		
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfinished_processing_message_number:{}", new Object[]{user_mail, accountId, phoneId, messageNumber});
	}

	//  ----- private function -----
	/*
	 * 保存账期表的内容（非账单、入库操作失败的、新模版的账单）
	 * 
	 * @param path1		path1[0] 原始邮件的拼写的串; path1[1] 原始邮件的拼写的串由DFS生成的返回 串 
	 * @param path2		path2[0] 截取后的邮件的拼写的串; path2[1] 截取后的邮件拼写的串由DFS生成的返回 串 
	 * @param detail	描述信息
	 */
	private void saveBillCycle(String[] path1, String[] path2, String detail) throws MailBillException{
		NotBillCycleInfoEntity entity = new NotBillCycleInfoEntity();
		entity.setSenderUrl(ReadProperty.getEmailUrl(path1[0],2));
		entity.setIsBill(MailBillTypeConstants.BILL_FALSE);//即非邮件账单：is_mailBill=false
		entity.setInfoSource(ReadProperty.getEmailUrl(path1[0],1));//获取用户邮箱
		entity.setOldHtmlUrl(path1[0]);
		entity.setOldHtmlDFS(path1[1]);
		entity.setNewHtmlUrl(path2[0]);
		entity.setNewHtmlDFS(path2[1]);
		String emailInfo = ReadProperty.getEmailInfo(path1[0], 2);
		if (!StringUtils.isBlank(emailInfo)){
			Date stringToDate = DateUtil.stringToDate(emailInfo, "yyyy-MM-dd HH:mm:ss");
			entity.setSentData(stringToDate);
		}
		entity.setReceiveAddUrl(ReadProperty.getEmailInfo(path1[0], 1));
		entity.setSubject(ReadProperty.getEmailInfo(path1[0], 0));
		entity.setDetail(detail);
		notBillCycleInfoServiceImpl.save(entity);
	}
}
