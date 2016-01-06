package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 邮件账单日账单明细
 * 
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_BANK_DAY_DETAIL")
public class BillBankDayDetailEntity extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5747088625629740920L;
	private int isMaster;				//主/附属卡（1：主卡；0：附属卡）
	private int incomeOrPay;			//是否为收入，1收入0支出（默认为0）
	private String cardEndOfFour;		//卡号末四位
	private Date merchandiseDate;		//交易日期
	private Date merchandiseTime;		//交易时间
	private String currencyType;		//币种
	private double merchandiseAmount;	//交易金额
	private String merchandiseDetail;	//交易摘要
	private String detail;				//信息描述
	private Long billCyclePkId;			//账单周期表主键
	private String viceCard;			//附属卡号（有附属卡必填）
	
	public BillBankDayDetailEntity(){
		
	}
	
	@Column(name="INCOME_OR_PAY")
	public int getIncomeOrPay() {
		return incomeOrPay;
	}
	public void setIncomeOrPay(int incomeOrPay) {
		this.incomeOrPay = incomeOrPay;
	}
	@Column(name="IS_MASTER")
	public int getIsMaster() {
		return isMaster;
	}
	public void setIsMaster(int isMaster) {
		this.isMaster = isMaster;
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
	@Column(name="MERCHANDISE_TIME")
	public Date getMerchandiseTime() {
		return merchandiseTime;
	}
	public void setMerchandiseTime(Date merchandiseTime) {
		this.merchandiseTime = merchandiseTime;
	}
	@Column(name="CURRENCY_TYPE")
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	@Column(name="MERCHANDISE_AMOUNT")
	public double getMerchandiseAmount() {
		return merchandiseAmount;
	}
	public void setMerchandiseAmount(double merchandiseAmount) {
		this.merchandiseAmount = merchandiseAmount;
	}
	@Column(name="MERCHANDISE_DETAIL")
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "billCyclePkId_id")
	@JoinColumn(name = "BILL_CYCLE_PK_ID")
	public Long getBillCyclePkId() {
		return billCyclePkId;
	}
	public void setBillCyclePkId(Long billCyclePkId) {
		this.billCyclePkId = billCyclePkId;
	}
	@Column(name="VICE_CARD")
	public String getViceCard() {
		return viceCard;
	}
	public void setViceCard(String viceCard) {
		this.viceCard = viceCard;
	}
}
