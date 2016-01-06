package net.umpay.mailbill.hql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 本期应还总额信息详情
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BALANCE_DETAIL")
public class BalanceDetailEntity extends IdEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5677481016515303341L;
	private double balance;			// 上期账单金额
	private double newCharges;		//'本期账单金额
	private double payment;			//上期还款金额'
	private double adjustment;		//'本期调整金额'
	private double interest;		//'循环利息
	private double newBalance;		//'本期应缴余额
	private Long billCyclePkId;		//账单周期表主键id
	private String detail;			//'描述信息
	private String currencyType;	//币种
	
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	@Column(name="new_charges")
	public double getNewCharges() {
		return newCharges;
	}
	public void setNewCharges(double newCharges) {
		this.newCharges = newCharges;
	}
	
	@Column(name="payment")
	public double getPayment() {
		return payment;
	}
	public void setPayment(double payment) {
		this.payment = payment;
	}
	
	@Column(name="ADJUSTMENT")
	public double getAdjustment() {
		return adjustment;
	}
	public void setAdjustment(double adjustment) {
		this.adjustment = adjustment;
	}
	
	@Column(name="INTEREST")
	public double getInterest() {
		return interest;
	}
	public void setInterest(double interest) {
		this.interest = interest;
	}
	
	@Column(name="NEW_BALANCE")
	public double getNewBalance() {
		return newBalance;
	}
	public void setNewBalance(double newBalance) {
		this.newBalance = newBalance;
	}
//	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity=BillCycleInfoEntity.class )
//	@JoinColumn(name = "BILL_CYCLE_PK_ID")
	@Column(name = "BILL_CYCLE_PK_ID")
	public Long getBillCyclePkId() {
		return billCyclePkId;
	}
	public void setBillCyclePkId(Long billCyclePkId) {
		this.billCyclePkId = billCyclePkId;
	}
	
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	@Column(name="CURRENCY_TYPE")
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
}
