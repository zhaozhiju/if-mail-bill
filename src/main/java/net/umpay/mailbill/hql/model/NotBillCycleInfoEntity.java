package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;
/**
 * 邮件账单周期表
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_NOT_BILL_CYCLE_INFO")
public class NotBillCycleInfoEntity extends IdEntity{

	private static final long serialVersionUID = 4620774818090942984L;
	private	String senderUrl;//用户发件人的邮箱
	private	String receiveAddUrl;//用户收件人的邮箱
	private	Date sentData;//用户收件人的时间
	private	String subject;//邮件主题
	private	String oldHtmlUrl;//原邮件HTML访问地址
	private	String newHtmlUrl;//截取拼接后HTML访问地址
	private	String oldHtmlDFS;//DFS分布式文件原始邮件HTML访问地址
	private	String newHtmlDFS;//DFS分布式文件截取拼接后HTML访问地址
	private	String infoSource;//邮箱信息来源
	private	String detail;//信息描述
	private Integer isBill;//是否为账单（0：否；1：是）
	private Integer isPush = 0;//是否推送到app（0：否；1：是）默认是0

	
	@Column(name="SENT_DATA")
	public Date getSentData() {
		return sentData;
	}
	@Column(name="SUBJECT")
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setSentData(Date sentData) {
		this.sentData = sentData;
	}
	@Column(name="RECEIVE_ADD_URL")
	public String getReceiveAddUrl() {
		return receiveAddUrl;
	}
	public void setReceiveAddUrl(String receiveAddUrl) {
		this.receiveAddUrl = receiveAddUrl;
	}
	@Column(name="OLD_HTML_DFS")
	public String getOldHtmlDFS() {
		return oldHtmlDFS;
	}
	public void setOldHtmlDFS(String oldHtmlDFS) {
		this.oldHtmlDFS = oldHtmlDFS;
	}
	@Column(name="NEW_HTML_DFS")
	public String getNewHtmlDFS() {
		return newHtmlDFS;
	}
	public void setNewHtmlDFS(String newHtmlDFS) {
		this.newHtmlDFS = newHtmlDFS;
	}
	@Column(name="SENDER_URL")
	public String getSenderUrl() {
		return senderUrl;
	}
	public void setSenderUrl(String senderUrl) {
		this.senderUrl = senderUrl;
	}
	@Column(name="OLD_HTML_URL")
	public String getOldHtmlUrl() {
		return oldHtmlUrl;
	}
	public void setOldHtmlUrl(String oldHtmlUrl) {
		this.oldHtmlUrl = oldHtmlUrl;
	}
	@Column(name="NEW_HTML_URL")
	public String getNewHtmlUrl() {
		return newHtmlUrl;
	}
	public void setNewHtmlUrl(String newHtmlUrl) {
		this.newHtmlUrl = newHtmlUrl;
	}
	@Column(name="INFO_SOURCE")
	public String getInfoSource() {
		return infoSource;
	}
	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	@Column(name="IS_BILL")
	public Integer getIsBill() {
		return isBill;
	}
	public void setIsBill(Integer isBill) {
		this.isBill = isBill;
	}
	@Column(name="IS_PUSH")
	public Integer getIsPush() {
		return isPush;
	}
	public void setIsPush(Integer isPush) {
		this.isPush = isPush;
	}
}
