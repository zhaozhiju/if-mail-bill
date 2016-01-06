package net.umpay.mailbill.api.model.viewpart;


public class DayBillView {
	
	private String cardEndOfFour;		//卡号末四位
	private String merchandiseDate;		//交易日（明细）
	private String merchandiseTime;		//交易时间
	private String currencyType;		//币种
	private String merchandiseAmount;	//交易金额
	private String merchandiseDetail;	//商户名称
	private int incomeOrPay;			//是否为收入，1收入0支出（默认为0）
	private int isMaster;				//主/附属卡（1：主卡；0：附属卡）
	private String detail;				//信息描述
	private Long billCyclePkId;			//账单周期表主键
	private String viceCard;			//附属卡号（有附属卡必填）
	
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
	public String getMerchandiseTime() {
		return merchandiseTime;
	}
	public void setMerchandiseTime(String merchandiseTime) {
		this.merchandiseTime = merchandiseTime;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	public String getMerchandiseAmount() {
		return merchandiseAmount;
	}
	public void setMerchandiseAmount(String merchandiseAmount) {
		this.merchandiseAmount = merchandiseAmount;
	}
	public String getMerchandiseDetail() {
		return merchandiseDetail;
	}
	public void setMerchandiseDetail(String merchandiseDetail) {
		this.merchandiseDetail = merchandiseDetail;
	}
	public int getIsMaster() {
		return isMaster;
	}
	public void setIsMaster(int isMaster) {
		this.isMaster = isMaster;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public Long getBillCyclePkId() {
		return billCyclePkId;
	}
	public void setBillCyclePkId(Long billCyclePkId) {
		this.billCyclePkId = billCyclePkId;
	}
	public String getViceCard() {
		return viceCard;
	}
	public void setViceCard(String viceCard) {
		this.viceCard = viceCard;
	}
	
	
}
