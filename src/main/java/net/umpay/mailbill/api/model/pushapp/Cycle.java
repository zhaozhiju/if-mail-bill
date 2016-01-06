package net.umpay.mailbill.api.model.pushapp;

import java.util.List;

/**
 * 账期表外层VO
 */
public class Cycle {

	private String dataType;				// 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	private String bankId;					// 银行id (对应卡数据)
	private String cardEndOfFour;			// 卡号末四位(对应卡数据)
	private String paymentDueDate;			// 还款日（yyyy/mm/dd）
	private String billType;				// 账单类型 ; 1月账单 2日账单
	private String cardUniqueNo; 			// 卡的唯一标示
	private List<CycleDetial> billcycleArr;	// 账期详情列表
	
	/**
	 * 获取dataType
	 * 
	 * @return dataType
	 */
	public String getDataType() {
		return dataType;
	}
	
	/**
	 * 获取dataType
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
	 * 获取账单类型 ; 1月账单 2日账单
	 * @return String
	 */
	public String getBillType() {
		return billType;
	}

	/**
	 * 设置账单类型 ; 1月账单 2日账单
	 * @param billType
	 */
	public void setBillType(String billType) {
		this.billType = billType;
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
	 * 获取账期详情列表
	 * 
	 * @return billcycleArr
	 */
	public List<CycleDetial> getBillcycleArr() {
		return billcycleArr;
	}
	
	/**
	 * 设置账期详情列表
	 * 
	 * @param billcycleArr
	 */
	public void setBillcycleArr(List<CycleDetial> billcycleArr) {
		this.billcycleArr = billcycleArr;
	}
	
}
