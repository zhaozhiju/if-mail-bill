package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 邮件账单用户信息
 * @author admin
 *
 */
@Entity
@Table(name="T_MAIL_BILL_USER_INFO")
public class BillUserInfoEntity extends IdEntity {
	private static final long serialVersionUID = 7800804273184425177L;
	private String emailUrl;	//邮箱地址
	private String password;	//邮箱密码
	private long accountId;		//用户标示
	private Date bindingDate;	//绑定时间
	private String detail;		//描述信息
	private int bindingStatus;	//邮箱绑定状态（1绑定，0解绑，默认1）
	
	@Column(name="EAMIL_URL")
	public String getEmailUrl() {
		return emailUrl;
	}
	public void setEmailUrl(String emailUrl) {
		this.emailUrl = emailUrl;
	}
	@Column(name="PASSWORD")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Column(name="ACCOUNT_ID")
	public long getAccountId() {
		return accountId;
	}
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	@Column(name="BINDING_DATE")
	public Date getBindingDate() {
		return bindingDate;
	}
	public void setBindingDate(Date bindingDate) {
		this.bindingDate = bindingDate;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	@Column(name="BINDING_STATUS")
	public int getBindingStatus() {
		return bindingStatus;
	}
	public void setBindingStatus(int bindingStatus) {
		this.bindingStatus = bindingStatus;
	}
}
