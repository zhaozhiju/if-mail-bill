package net.umpay.mailbill.api.model.pushapp;

/**
 * 月账单详情VO 
 */
public class MonthBillDetial {

	private int incomeOrPay;			// 是否为收入，1收入0支出（默认为0）
	private String cardEndOfFour;		// 卡号末四位
	private String merchandiseDate;		// 交易日（明细）
	private String postDate;			// 记账日
	private String merchandiseDetail;	// 交易摘要
	private String currencyType;		// 币种
	private double amount;				// 交易金额
	private String merchandiseArea;		// 交易地点
	private double originalTransAmount;	// 交易地金额
	
	/**
	 * 获取是否为收入
	 * 
	 * @return incomeOrPay
	 */
	public int getIncomeOrPay() {
		return incomeOrPay;
	}
	
	/**
	 * 设置是否为收入
	 * 
	 * @param incomeOrPay
	 */
	public void setIncomeOrPay(int incomeOrPay) {
		this.incomeOrPay = incomeOrPay;
	}
	
	/**
	 * 获取卡号末四位
	 * 
	 * @return cardEndOfFour
	 */
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	
	/**
	 * 设置卡号末四位
	 * 
	 * @param cardEndOfFour
	 */
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	
	/**
	 * 获取交易日（明细）
	 * 
	 * @return merchandiseDate
	 */
	public String getMerchandiseDate() {
		return merchandiseDate;
	}
	
	/**
	 * 设置交易日（明细）
	 * 
	 * @param merchandiseDate
	 */
	public void setMerchandiseDate(String merchandiseDate) {
		this.merchandiseDate = merchandiseDate;
	}
	
	/**
	 * 获取记账日
	 * 
	 * @return postDate
	 */
	public String getPostDate() {
		return postDate;
	}
	
	/**
	 * 设置记账日
	 * 
	 * @param postDate
	 */
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	
	/**
	 * 获取交易摘要
	 * 
	 * @return merchandiseDetail
	 */
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	
	/**
	 * 设置交易摘要
	 * 
	 * @param merchandiseDetail
	 */
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
	
	/**
	 * 获取币种
	 * 
	 * @return currencyType
	 */
	public String getCurrencyType() {
		return currencyType;
	}
	
	/**
	 * 设置币种
	 * 
	 * @param currencyType
	 */
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	
	/**
	 * 获取交易金额
	 * 
	 * @return amount
	 */
	public double getAmount() {
		return amount;
	}
	
	/**
	 * 设置交易金额
	 * 
	 * @param amount
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	/**
	 * 获取交易地点
	 * 
	 * @return merchandiseArea
	 */
	public String getMerchandiseArea() {
		return merchandiseArea;
	}
	
	/**
	 * 设置交易地点
	 * 
	 * @param merchandiseArea
	 */
	public void setMerchandiseArea(String merchandiseArea) {
		this.merchandiseArea = merchandiseArea;
	}
	
	/**
	 * 获取交易地金额
	 * 
	 * @return originalTransAmount
	 */
	public double getOriginalTransAmount() {
		return originalTransAmount;
	}
	
	/**
	 * 设置交易地金额
	 * 
	 * @param originalTransAmount
	 */
	public void setOriginalTransAmount(double originalTransAmount) {
		this.originalTransAmount = originalTransAmount;
	}
}
