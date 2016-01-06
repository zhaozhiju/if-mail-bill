package net.umpay.mailbill.hql.model;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.umpay.mailbill.entrance.websocket.MyWebSocket;
import net.umpay.mailbill.service.impl.banktemplate.ParseMailService;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

/**
 * 子线程开启
 */
public class ThreadRunAbleTest extends MyWebSocket implements Runnable{

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(ThreadRunAbleTest.class);
	
	private String subject;
	private String receiveAdd;
	private StringBuffer content;
	private String sentData;
	private String senderAdd;
	private String user_mail;
	private int messageNumber;
	private String key;
	private String accountidPhoneId;
	private Map<String, BillLogEntity> logMap;
	private int threadNum;
	private CountDownLatch countDownLatch;
	private ParseMailService mailService;
	private Long scVersion;
	
	public ThreadRunAbleTest(ParseMailService mailService, CountDownLatch countDownLatch, String subject, String receiveAdd, StringBuffer content, String sentData, String senderAdd
			, String user_mail, int messageNumber, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap, int threadNum, Long scVersion) {
		
		this.mailService = mailService;
		this.countDownLatch = countDownLatch;
		this.content = content;
		this.key = key;
		this.threadNum = threadNum;
		this.accountidPhoneId = accountid_phoneId;
		this.logMap = logMap;
		this.messageNumber = messageNumber;
		this.receiveAdd = receiveAdd;
		this.senderAdd = senderAdd;
		this.sentData = sentData;
		this.subject = subject;
		this.user_mail = user_mail;
		this.scVersion = scVersion;
	}
	
	
	@Override
	public void run() {
		try {
			mailService.downloadMail(subject,receiveAdd,content,sentData,senderAdd,user_mail,messageNumber, key, accountidPhoneId, logMap, scVersion);
		} catch (MailBillException e) {
			MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		} finally{
			countDownLatch.countDown(); // 如果计数到达零,释放所有等待的线程
			long countDown = threadNum - countDownLatch.getCount();
			this.send("C1`已完成：" + countDown + "封下载解析", key);	
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getReceiveAdd() {
		return receiveAdd;
	}

	public void setReceiveAdd(String receiveAdd) {
		this.receiveAdd = receiveAdd;
	}

	public StringBuffer getContent() {
		return content;
	}

	public void setContent(StringBuffer content) {
		this.content = content;
	}

	public String getSentData() {
		return sentData;
	}

	public void setSentData(String sentData) {
		this.sentData = sentData;
	}

	public String getSenderAdd() {
		return senderAdd;
	}

	public void setSenderAdd(String senderAdd) {
		this.senderAdd = senderAdd;
	}

	public String getUser_mail() {
		return user_mail;
	}

	public void setUser_mail(String user_mail) {
		this.user_mail = user_mail;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getAccountidPhoneId() {
		return accountidPhoneId;
	}

	public void setAccountidPhoneId(String accountidPhoneId) {
		this.accountidPhoneId = accountidPhoneId;
	}

	public Map<String, BillLogEntity> getLogMap() {
		return logMap;
	}

	public void setLogMap(Map<String, BillLogEntity> logMap) {
		this.logMap = logMap;
	}

	
	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public ParseMailService getMailService() {
		return mailService;
	}

	public void setMailService(ParseMailService mailService) {
		this.mailService = mailService;
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}
	
	public Long getScVersion() {
		return scVersion;
	}

	public void setScVersion(Long scVersion) {
		this.scVersion = scVersion;
	}
}
