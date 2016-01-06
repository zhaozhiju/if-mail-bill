package net.umpay.mailbill.hql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 邮箱引导统计信息(pop3/imap4的开启提示)
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_LEAD_INFO")
public class LeadInfoEntity extends IdEntity{

	private static final long serialVersionUID = -6042771044973872843L;
	
	private String emailUrl;	//Email地址
	private int pop3IsOpen;		//pop3是否打开
	private int imap4IsOpen;	//imap4是否打开
	private String detail;		//描述信息
	
	@Column(name="email_url")
	public String getEmailUrl() {
		return emailUrl;
	}
	public void setEmailUrl(String emailUrl) {
		this.emailUrl = emailUrl;
	}
	
	@Column(name="pop3_is_open")
	public int getPop3IsOpen() {
		return pop3IsOpen;
	}
	public void setPop3IsOpen(int pop3IsOpen) {
		this.pop3IsOpen = pop3IsOpen;
	}
	
	@Column(name="imap4_is_open")
	public int getImap4IsOpen() {
		return imap4IsOpen;
	}
	public void setImap4IsOpen(int imap4IsOpen) {
		this.imap4IsOpen = imap4IsOpen;
	}
	
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
}
