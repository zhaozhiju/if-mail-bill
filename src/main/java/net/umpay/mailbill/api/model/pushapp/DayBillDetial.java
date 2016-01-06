package net.umpay.mailbill.api.model.pushapp;

/**
 * 日账单详情VO
 * 
 */
public class DayBillDetial {

	private String cardEndOfFour;		// 卡号末四位
	private String merchandiseDate;		// 交易日（明细）
	private String merchandiseTime;		// 交易时间
	private String currencyType;		// 币种
	private String merchandiseAmount;	// 交易金额
	private String merchandiseDetail;	// 商户名称
	private int incomeOrPay;			// 是否为收入，1收入 0支出（默认为0）
	private String detail;				// 信息描述
	//private long billCyclePkId;			// 账单周期表主键
	
	/**
	 * 获取卡号末四位
	 * 
	 * @return String
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
	 * 获取交易时间
	 * 
	 * @return merchandiseTime
	 */
	public String getMerchandiseTime() {
		return merchandiseTime;
	}
	
	/**
	 * 设置交易时间
	 * 
	 * @param merchandiseTime
	 */
	public void setMerchandiseTime(String merchandiseTime) {
		this.merchandiseTime = merchandiseTime;
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
	 * @return merchandiseAmount
	 */
	public String getMerchandiseAmount() {
		return merchandiseAmount;
	}
	
	/**
	 * 设置交易金额
	 * 
	 * @param merchandiseAmount
	 */
	public void setMerchandiseAmount(String merchandiseAmount) {
		this.merchandiseAmount = merchandiseAmount;
	}
	
	/**
	 * 获取商户名称
	 * 
	 * @return merchandiseDetail
	 */
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	
	/**
	 * 设置商户名称
	 * 
	 * @param merchandiseDetail
	 */
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
	
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
	 * 获取信息描述
	 * 
	 * @return detail
	 */
	public String getDetail() {
		return detail;
	}
	
	/**
	 * 设置信息描述
	 * 
	 * @param detail
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}
	
	/**
	 * 获取账单周期表主键
	 * 
	 * @return billCyclePkId
	 
	public long getBillCyclePkId() {
		return billCyclePkId;
	}*/
	
	/**
	 * 设置账单周期表主键
	 * 
	 * @param billCyclePkId
	 
	public void setBillCyclePkId(long billCyclePkId) {
		this.billCyclePkId = billCyclePkId;
	}*/
}
