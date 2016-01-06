package net.umpay.mailbill.api.model.pushapp;

import java.util.List;

/**
 * 月账单外层VO 
 */
public class MonthBill {

	private String dataType;					// 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表6 解绑邮箱成功/失败
	private String bankId;						// 银行id (对应卡数据)
	private String cardEndOfFour;				// 卡号末四位(对应卡数据)
	private String paymentDueDate;				// 还款日（yyyy/mm/dd）
	private String accountOfDate;				// 账期(对应账期数据)
	private long billcycleId;					// 账单周期表主键
	private String cardUniqueNo; 				// 卡的唯一标示
	private List<MonthBillDetial> monthbillArr;	// 月账单详情列表
	
	/**
	 * 获取dataType
	 * 
	 * @return dataType
	 */
	public String getDataType() {
		return dataType;
	}
	
	/**
	 * 设置dataType
	 * 
	 * @param dataType
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	/**
	 * 获取银行id (对应卡数据)
	 * 
	 * @return bankId
	 */
	public String getBankId() {
		return bankId;
	}
	
	/**
	 * 设置银行id (对应卡数据)
	 * 
	 * @param bankId
	 */
	public void setBankId(String bankId) {
		this.bankId = bankId;
	}
	
	/**
	 * 获取卡号末四位(对应卡数据)
	 * 
	 * @return cardEndOfFour
	 */
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	
	/**
	 * 设置卡号末四位(对应卡数据)
	 * 
	 * @param cardEndOfFour
	 */
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	
	/**
	 * 获取还款日（yyyy/mm/dd）
	 * @return String
	 */
	public String getPaymentDueDate() {
		return paymentDueDate;
	}

	/**
	 * 设置还款日（yyyy/mm/dd）
	 * @param paymentDueDate
	 */
	public void setPaymentDueDate(String paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	/**
	 * 获取账期(对应账期数据)
	 * 
	 * @return String
	 */
	public String getAccountOfDate() {
		return accountOfDate;
	}
	
	/**
	 * 设置账期(对应账期数据)
	 * 
	 * @param accountOfDate
	 */
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}
	
	/**
	 * 获取账单周期表主键
	 * @return long
	 */
	public long getBillcycleId() {
		return billcycleId;
	}

	/**
	 * 设置账单周期表主键
	 * @param billcycleId
	 */
	public void setBillcycleId(long billcycleId) {
		this.billcycleId = billcycleId;
	}

	/**
	 * 获取卡的唯一标示
	 * @return String
	 */
	public String getCardUniqueNo() {
		return cardUniqueNo;
	}

	/**
	 * 设置卡的唯一标示
	 * @param cardUniqueNo
	 */
	public void setCardUniqueNo(String cardUniqueNo) {
		this.cardUniqueNo = cardUniqueNo;
	}
	
	/**
	 * 获取月账单详情列表
	 * 
	 * @return monthbillArr
	 */
	public List<MonthBillDetial> getMonthbillArr() {
		return monthbillArr;
	}
	
	/**
	 * 设置月账单详情列表
	 * 
	 * @param monthbillArr
	 */
	public void setMonthbillArr(List<MonthBillDetial> monthbillArr) {
		this.monthbillArr = monthbillArr;
	}
	
}
