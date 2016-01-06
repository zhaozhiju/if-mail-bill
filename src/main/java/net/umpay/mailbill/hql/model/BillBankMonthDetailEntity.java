package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 邮件账单月账单明细
 * 
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_BANK_MONTH_DETAIL")
public class BillBankMonthDetailEntity extends IdEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1317739381622812596L;
	private int isMaster;					//'主/附属卡（1：主卡；2：附属卡）
	private int incomeOrPay;				//收入或者支出
	private String cardEndOfFour;			//卡号末四位
	private Date merchandiseDate;			//交易日（明细）
	private Date postDate;					//记账日（明细）
	private String merchandiseDetail;		//交易摘要（明细）
	private String currencyType;			//币种
	private double amount;					//交易金额（明细）
	private String merchandiseArea;			//交易地点（明细）
	private double originalTransAmount;		//交易地金额（明细）
	private String detail;					//信息描述
	private Long billCyclePkId;				//账单周期表主键
	
	@Column(name="IS_MASTER")
	public int getIsMaster() {
		return isMaster;
	}
	public void setIsMaster(int isMaster) {
		this.isMaster = isMaster;
	}
	
	@Column(name="INCOME_OR_PAY")
	public int getIncomeOrPay() {
		return incomeOrPay;
	}
	public void setIncomeOrPay(int incomeOrPay) {
		this.incomeOrPay = incomeOrPay;
	}
	@Column(name="CARD_END_OF_FOUR")
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	
	@Column(name="MERCHANDISE_DATE")
	public Date getMerchandiseDate() {
		return merchandiseDate;
	}
	public void setMerchandiseDate(Date merchandiseDate) {
		this.merchandiseDate = merchandiseDate;
	}
	
	@Column(name="POST_DATE")
	public Date getPostDate() {
		return postDate;
	}
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	
	@Column(name="MERCHANDISE_DETAIL")
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
	
	@Column(name="CURRENCY_TYPE")
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	@Column(name="MERCHANDISE_AREA")
	public String getMerchandiseArea() {
		return merchandiseArea;
	}
	public void setMerchandiseArea(String merchandiseArea) {
		this.merchandiseArea = merchandiseArea;
	}
	
	@Column(name="ORIGINAL_TRANS_AMOUNT")
	public double getOriginalTransAmount() {
		return originalTransAmount;
	}
	public void setOriginalTransAmount(double originalTransAmount) {
		this.originalTransAmount = originalTransAmount;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name="billCyclePkId_id")
	@Column(name="BILL_CYCLE_PK_ID")
	public Long getBillCyclePkId() {
		return billCyclePkId;
	}
	public void setBillCyclePkId(Long billCyclePkId) {
		this.billCyclePkId = billCyclePkId;
	}
	
}
