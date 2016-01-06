package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 推送关系表
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_PUSH")
public class BillPushEntity extends IdEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long accountId;
	private String billCyclePkIds;
	private boolean isPush;
	private Date  createTime;
	
	
	@Column(name="ACCOUNT_ID")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	@Column(name="BILL_CYCLE_PK_IDS")
	public String getBillCyclePkIds() {
		return billCyclePkIds;
	}
	public void setBillCyclePkIds(String billCyclePkIds) {
		this.billCyclePkIds = billCyclePkIds;
	}
	@Column(name="IS_PUSH")
	public boolean isPush() {
		return isPush;
	}
	public void setPush(boolean isPush) {
		this.isPush = isPush;
	}
	@Column(name="CREATE_TIME")
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
