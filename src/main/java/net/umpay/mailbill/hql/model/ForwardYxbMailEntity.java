package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 自建邮箱服务器的关系表
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_FORWARD_YXB_MAIL")
public class ForwardYxbMailEntity extends IdEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mailName;
	private String yxbMailName;
	private String yxbMailPassword;
	private Long accountId;
	private Date  createTime;
	
	@Column(name="MAIL_NAME")
	public String getMailName() {
		return mailName;
	}
	public void setMailName(String mailName) {
		this.mailName = mailName;
	}
	@Column(name="YXB_MAIL_NAME")
	public String getYxbMailName() {
		return yxbMailName;
	}
	public void setYxbMailName(String yxbMailName) {
		this.yxbMailName = yxbMailName;
	}
	@Column(name="YXB_MAIL_PASSWORD")
	public String getYxbMailPassword() {
		return yxbMailPassword;
	}
	public void setYxbMailPassword(String yxbMailPassword) {
		this.yxbMailPassword = yxbMailPassword;
	}
	@Column(name="ACCOUNT_ID")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	@Column(name="CREATE_TIME")
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
