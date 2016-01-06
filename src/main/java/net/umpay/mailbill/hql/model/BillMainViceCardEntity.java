package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 主副卡表关系设置
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_MAIN_VICE_CARD")
public class BillMainViceCardEntity extends IdEntity{
	
	private static final long serialVersionUID = -4622435593968963022L;
	private Long accountId;			// 用户账号
	private	Integer bankId;			// 银行类型（银行主键id）
	private	String infoSourceEmail;	// 邮箱信息来源
	private	String mainCardOfFour;	// 主卡卡号
	private	String viceCardOfFour;	// 副卡卡号
	private Date  createTime;		// 主副卡创建关系时间
	private Date  updateTime;		// 更新关系时间
	
	
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
	@Column(name="BANK_ID")
	public Integer getBankId() {
		return bankId;
	}
	public void setBankId(Integer bankId) {
		this.bankId = bankId;
	}
	@Column(name="INFO_SOURCE_EMAIL")
	public String getInfoSourceEmail() {
		return infoSourceEmail;
	}
	public void setInfoSourceEmail(String infoSourceEmail) {
		this.infoSourceEmail = infoSourceEmail;
	}
	@Column(name="MAIN_CARD_OF_FOUR")
	public String getMainCardOfFour() {
		return mainCardOfFour;
	}
	public void setMainCardOfFour(String mainCardOfFour) {
		this.mainCardOfFour = mainCardOfFour;
	}
	@Column(name="VICE_CARD_OF_FOUR")
	public String getViceCardOfFour() {
		return viceCardOfFour;
	}
	public void setViceCardOfFour(String viceCardOfFour) {
		this.viceCardOfFour = viceCardOfFour;
	}
	@Column(name="UPDATE_TIME")
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
