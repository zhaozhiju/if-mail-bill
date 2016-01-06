package net.umpay.mailbill.api.model.viewpart;

/**
 * 积分汇总信息
 * 
 * @author admin
 *
 */
public class IntegrationBillView {

	private int balancePoints;//上期积分余额
	private int addedPoints;//本期新增积分
	private int revisePoints;//本期调整积分
	private int awardPoints;//本期奖励积分
	private int exchangePoints;//本期兑换积分总数
	private int usePoints;//可用积分余额
	private String impendingFailurePoints;//即将失效积分
	private int tourismPoints;		//旅游
	private String currencyType;//币种
	
	public int getTourismPoints() {
		return tourismPoints;
	}
	public void setTourismPoints(int tourismPoints) {
		this.tourismPoints = tourismPoints;
	}
	public String getImpendingFailurePoints() {
		return impendingFailurePoints;
	}
	public void setImpendingFailurePoints(String impendingFailurePoints) {
		this.impendingFailurePoints = impendingFailurePoints;
	}
	public int getBalancePoints() {
		return balancePoints;
	}
	public void setBalancePoints(int balancePoints) {
		this.balancePoints = balancePoints;
	}
	public int getAddedPoints() {
		return addedPoints;
	}
	public void setAddedPoints(int addedPoints) {
		this.addedPoints = addedPoints;
	}
	public int getRevisePoints() {
		return revisePoints;
	}
	public void setRevisePoints(int revisePoints) {
		this.revisePoints = revisePoints;
	}
	public int getAwardPoints() {
		return awardPoints;
	}
	public void setAwardPoints(int awardPoints) {
		this.awardPoints = awardPoints;
	}
	public int getExchangePoints() {
		return exchangePoints;
	}
	public void setExchangePoints(int exchangePoints) {
		this.exchangePoints = exchangePoints;
	}
	public int getUsePoints() {
		return usePoints;
	}
	public void setUsePoints(int usePoints) {
		this.usePoints = usePoints;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	
	
}
