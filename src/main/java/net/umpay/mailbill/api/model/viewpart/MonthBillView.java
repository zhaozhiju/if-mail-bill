package net.umpay.mailbill.api.model.viewpart;


/**
 * 月账单详细交易
 * 
 * @author admin
 *
 */
public class MonthBillView {
	
	private int isMaster;//'主/附属卡（1：主卡；0：附属卡）
	private int incomeOrPay;//是否为收入，1收入0支出（默认为0）
	private String cardEndOfFour;//卡号末四位
	private String merchandiseDate;//交易日（明细）
	private String postDate;//记账日（明细）
	private String merchandiseDetail;//交易摘要（明细）
	private String currencyType;//币种
	private double amount;//交易金额（明细）
	private String merchandiseArea;//交易地点（明细）
	private double originalTransAmount;//交易地金额（明细）
	
	public int getIsMaster() {
		return isMaster;
	}
	public void setIsMaster(int isMaster) {
		this.isMaster = isMaster;
	}
	public int getIncomeOrPay() {
		return incomeOrPay;
	}
	public void setIncomeOrPay(int incomeOrPay) {
		this.incomeOrPay = incomeOrPay;
	}
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	public String getMerchandiseDate() {
		return merchandiseDate;
	}
	public void setMerchandiseDate(String merchandiseDate) {
		this.merchandiseDate = merchandiseDate;
	}
	public String getPostDate() {
		return postDate;
	}
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
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
	public String getMerchandiseArea() {
		return merchandiseArea;
	}
	public void setMerchandiseArea(String merchandiseArea) {
		this.merchandiseArea = merchandiseArea;
	}
	public double getOriginalTransAmount() {
		return originalTransAmount;
	}
	public void setOriginalTransAmount(double originalTransAmount) {
		this.originalTransAmount = originalTransAmount;
	}
	
}
