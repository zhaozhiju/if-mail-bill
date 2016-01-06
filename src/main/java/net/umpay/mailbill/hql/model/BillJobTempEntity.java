package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 任务临时表
 * @author admin
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_JOB_TEMP")
public class BillJobTempEntity extends IdEntity{
	
	private static final long serialVersionUID = 2604537677085069957L;
	
	private Long accountId;			//用户的标示
	private int bankId;				//银行标示
	private String cardEndOfFour;	//卡号末四位
	private String userEmail;		//用户邮箱地址
	private int billDate;			//账单日
	private int billType;			//账单类型
	private Date billStartDate;		//账单搜索的开始之间
	private Date billEndDate;		//搜索账单的最新时间
	private int isVaild;			//搜索账单是否有效
	
	@Column(name="ACCOUNT_ID", nullable = true)
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
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
	@Column(name="bill_Start_Date", nullable = true)
	public Date getBillStartDate() {
		return billStartDate;
	}
	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
	@Column(name="BILL_END_DATE")
	public Date getBillEndDate() {
		return billEndDate;
	}
	public void setBillEndDate(Date billEndDate) {
		this.billEndDate = billEndDate;
	}
	@Column(name="IS_VAILD")
	public int getIsVaild() {
		return isVaild;
	}
	public void setIsVaild(int isVaild) {
		this.isVaild = isVaild;
	}
}
