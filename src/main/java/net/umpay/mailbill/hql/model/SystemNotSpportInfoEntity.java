package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 系统未兼容的邮箱
 * 
 * @author admin
 *
 */
@Entity
@Table(name="T_MAIL_SYSTEM_NOT_SPPORT_INFO")
public class SystemNotSpportInfoEntity extends IdEntity{
	
	private static final long serialVersionUID = -8741561455142734001L;
	
	private String emailSuffix;	//注册邮箱的后缀
	private Date bindingDate;	//最后绑定的日期
	private int bindingNumber;	//绑定失败次数
	private String detail;		//描述信息
	
	
	@Column(name="EMAIL_SUFFIX")
	public String getEmailSuffix() {
		return emailSuffix;
	}
	public void setEmailSuffix(String emailSuffix) {
		this.emailSuffix = emailSuffix;
	}
	
	
	@Column(name="BINDING_DATE")
	public Date getBindingDate() {
		return bindingDate;
	}
	public void setBindingDate(Date bindingDate) {
		this.bindingDate = bindingDate;
	}
	
	
	@Column(name="BINDING_NUMBER")
	public int getBindingNumber() {
		return bindingNumber;
	}
	public void setBindingNumber(int bindingNumber) {
		this.bindingNumber = bindingNumber;
	}
	
	
	@Column(name="DETAIL")
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
}
