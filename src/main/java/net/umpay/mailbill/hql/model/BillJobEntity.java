package net.umpay.mailbill.hql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 定时任务接口之一
 * 
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_JOB")
public class BillJobEntity extends IdEntity{
	
	private static final long serialVersionUID = 2604537677085069957L;
	
	private int bankId;				//银行标示
	private String cardEndOfFour;	//卡号末四位
	private String userEmail;		//用户邮箱地址
	private int billDate;			//账单日
	private int billType;			//账单类型

	@Column(name="BANK_ID", nullable = true)
	public int getBankId() {
		return bankId;
	}
	public void setBankId(int bankId) {
		this.bankId = bankId;
	}
	@Column(name="CARD_END_OF_FOUR", nullable = true)
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	@Column(name="USER_EMAIL", nullable = true)
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	@Column(name="BILL_DATE")
	public int getBillDate() {
		return billDate;
	}
	public void setBillDate(int billDate) {
		this.billDate = billDate;
	}
	@Column(name="BILL_TYPE", nullable = true)
	public int getBillType() {
		return billType;
	}
	public void setBillType(int billType) {
		this.billType = billType;
	}
	
}
