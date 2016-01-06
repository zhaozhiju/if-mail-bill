package net.umpay.mailbill.hql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 本期积分汇总详情
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_INTEGRATION_DETAIL")
public class IntegrationDetailEntity extends IdEntity{
	
	private static final long serialVersionUID = 2604537677085069957L;
	
	private int balancePoints;					// 上期积分余额
	private int addedPoints;					// 本期新增积分
	private int revisePoints;					// 本期调整积分
	private int awardPoints;					// 本期奖励积分
	private int exchangePoints;					// 本期兑换积分总数
	private int usePoints;						// 可用积分余额
	private String impendingFailurePoints;		// 即将失效积分
	private int tourismPoints;					// 旅游积分
	private Long billCyclePkId;					// 账单周期表主键
	private String detail;						// 描述信息
	private String currencyType;				// 币种
	
	@Column(name="BALANCE_POINTS")
	public int getBalancePoints() {
		return balancePoints;
	}
	public void setBalancePoints(int balancePoints) {
		this.balancePoints = balancePoints;
	}
	@Column(name="ADDED_POINTS")
	public int getAddedPoints() {
		return addedPoints;
	}
	public void setAddedPoints(int addedPoints) {
		this.addedPoints = addedPoints;
	}
	@Column(name="REVISE_POINTS")
	public int getRevisePoints() {
		return revisePoints;
	}
	public void setRevisePoints(int revisePoints) {
		this.revisePoints = revisePoints;
	}
	@Column(name="AWARD_POINTS")
	public int getAwardPoints() {
		return awardPoints;
	}
	public void setAwardPoints(int awardPoints) {
		this.awardPoints = awardPoints;
	}
	@Column(name="EXCHANGE_POINTS")
	public int getExchangePoints() {
		return exchangePoints;
	}
	public void setExchangePoints(int exchangePoints) {
		this.exchangePoints = exchangePoints;
	}
	@Column(name="USE_POINTS")
	public int getUsePoints() {
		return usePoints;
	}
	public void setUsePoints(int usePoints) {
		this.usePoints = usePoints;
	}
	@Column(name="BILL_CYCLE_PK_ID")
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
	@Column(name="IMPENDING_FAILURE_POINTS")
	public String getImpendingFailurePoints() {
		return impendingFailurePoints;
	}
	public void setImpendingFailurePoints(String impendingFailurePoints) {
		this.impendingFailurePoints = impendingFailurePoints;
	}
	@Column(name="TOURISM_POINTS")
	public int getTourismPoints() {
		return tourismPoints;
	}
	public void setTourismPoints(int tourismPoints) {
		this.tourismPoints = tourismPoints;
	}
	
}
