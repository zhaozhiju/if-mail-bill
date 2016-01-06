package net.umpay.mailbill.api.model.pushapp;

import java.util.List;

/**
 * 日账单外层VO 
 */
public class DayBill {

	private String dataType; 					// 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表6 解绑邮箱成功/失败 
	private String bankId;						// 银行id (对应卡数据)
	private String cardEndOfFour; 				// 卡号末四位(对应卡数据)
	private String merchandiseDate;				// 交易日（明细）
	private long billcycleId;					// 账单周期表主键
	private String cardUniqueNo; 				// 卡的唯一标示
	private List<DayBillDetial> datebillArr; 	// 日账单详情列表
	
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
	 * 获取日账单详情列表
	 * 
	 * @return datebillArr
	 */
	public List<DayBillDetial> getDatebillArr() {
		return datebillArr;
	}
	
	/**
	 * 设置日账单详情列表
	 * 
	 * @param datebillArr
	 */
	public void setDatebillArr(List<DayBillDetial> datebillArr) {
		this.datebillArr = datebillArr;
	}
	
}
