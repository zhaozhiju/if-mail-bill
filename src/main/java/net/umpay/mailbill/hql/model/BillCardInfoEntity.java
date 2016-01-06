package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 卡信息表
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_CARD_INFO")
public class BillCardInfoEntity extends IdEntity{

	private static final long serialVersionUID = -5981198253840606843L;
	private	Integer bankId;//银行类型（银行主键id）
	private Long accountId;	//用户账号
	private	String infoSourceEmail;//邮箱信息来源
	private	String mainCardOfFour;//主卡卡号
	private	Integer billDate;//账单日
	private	Date paymentDueDate;//到期还款日
	private	double rmbCreditLimit;//人民币信用额度
	private	double usaCreditLimit;//美元信用额度
	private	String cardType;//银行卡品牌（如：VISA。。。）
	private	String cardSpecies;//卡种
	private	double cashUsaAdvanceLimit;//美元预借现金额度
	private	double cashRmbAdvanceLimit;//人民币预借现金额度
	private	String userName;//用户姓名
	private	String userGender;//用户性别
	
	@Column(name="CASH_USA_ADVANCE_LIMIT")
	public double getCashUsaAdvanceLimit() {
		return cashUsaAdvanceLimit;
	}
	public void setCashUsaAdvanceLimit(double cashUsaAdvanceLimit) {
		this.cashUsaAdvanceLimit = cashUsaAdvanceLimit;
	}
	@Column(name="CASH_RMB_ADVANCE_LIMIT")
	public double getCashRmbAdvanceLimit() {
		return cashRmbAdvanceLimit;
	}
	public void setCashRmbAdvanceLimit(double cashRmbAdvanceLimit) {
		this.cashRmbAdvanceLimit = cashRmbAdvanceLimit;
	}
	@Column(name="USER_NAME")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Column(name="USER_GENDER")
	public String getUserGender() {
		return userGender;
	}
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	@Column(name="BANK_ID")
	public Integer getBankId() {
		return bankId;
	}
	public void setBankId(Integer bankId) {
		this.bankId = bankId;
	}
	@Column(name="ACCOUNT_ID")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
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
	@Column(name="BILL_DATE")
	public Integer getBillDate() {
		return billDate;
	}
	public void setBillDate(Integer billDate) {
		this.billDate = billDate;
	}
	@Column(name="PAYMENT_DUE_DATE")
	public Date getPaymentDueDate() {
		return paymentDueDate;
	}
	public void setPaymentDueDate(Date paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	@Column(name="RMB_CREDIT_LIMIT")
	public double getRmbCreditLimit() {
		return rmbCreditLimit;
	}
	public void setRmbCreditLimit(double rmbCreditLimit) {
		this.rmbCreditLimit = rmbCreditLimit;
	}
	@Column(name="USA_CREDIT_LIMIT")
	public double getUsaCreditLimit() {
		return usaCreditLimit;
	}
	public void setUsaCreditLimit(double usaCreditLimit) {
		this.usaCreditLimit = usaCreditLimit;
	}
	@Column(name="CARD_TYPE")
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	@Column(name="CARD_SPECIES")
	public String getCardSpecies() {
		return cardSpecies;
	}
	public void setCardSpecies(String cardSpecies) {
		this.cardSpecies = cardSpecies;
	}

	
}
