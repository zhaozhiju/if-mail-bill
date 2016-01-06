package net.umpay.mailbill.api.model.pushapp;

/**
 * 卡信息详情VO
 */
public class CardDetial {

	private String bankId;				// 银行ID
	private String cardEndOfFour;		// 卡号末四位
	private String accountOfDate;		// 账期(1月账单: yyyy/mm; 2日账单:yyyy/mm/dd)
	private String userName;			// 用户姓名
	private String userGender;			// 用户性别
	private String infoSource;			// 邮箱信息来源
	private long scVersion;				// 服务端数据版本号
	private String billType;			// 账单类型， 1月账单 2日账单
	private double rmbCreditLimit;		// 人民币信用额度
	private double usaCreditLimit;		// 美元信用额度
	private String paymentDueDate;		// 还款日
	private double cashUsaAdvanceLimit;	// 美元预借现金额度
	private double cashRmbAdvanceLimit;	// 人民币预借现金额度
	private String cardUniqueNo; 		// 卡的唯一标示
	
	/**
	 * 获取账期(1月账单: yyyy/mm; 2日账单:yyyy/mm/dd)
	 * 
	 * @return accountOfDate
	 */
	public String getAccountOfDate() {
		return accountOfDate;
	}

	/**
	 * 设置账期(1月账单: yyyy/mm; 2日账单:yyyy/mm/dd)
	 * 
	 * @param accountOfDate
	 */
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}

	/**
	 * 获取账单类型（1月账单 2日账单）
	 * 
	 * @return billType
	 */
	public String getBillType() {
		return billType;
	}

	/**
	 * 设置账单类型（1月账单 2日账单）
	 * 
	 * @param billType
	 */
	public void setBillType(String billType) {
		this.billType = billType;
	}

	/**
	 * 获取银行ID
	 * 
	 * @return bankId
	 */
	public String getBankId() {
		return bankId;
	}
	
	/**
	 * 设置银行ID
	 * 
	 * @param bankId
	 */
	public void setBankId(String bankId) {
		this.bankId = bankId;
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
	 * 获取人民币信用额度
	 * 
	 * @return double
	 */
	public double getRmbCreditLimit() {
		return rmbCreditLimit;
	}
	
	/**
	 * 设置人民币信用额度
	 * 
	 * @param rmbCreditLimit
	 */
	public void setRmbCreditLimit(double rmbCreditLimit) {
		this.rmbCreditLimit = rmbCreditLimit;
	}
	
	/**
	 * 获取美元信用额度
	 * 
	 * @return usaCreditLimit
	 */
	public double getUsaCreditLimit() {
		return usaCreditLimit;
	}
	
	/**
	 * 设置美元信用额度
	 * 
	 * @param usaCreditLimit
	 */
	public void setUsaCreditLimit(double usaCreditLimit) {
		this.usaCreditLimit = usaCreditLimit;
	}
	
	
	/**
	 * 获取还款日
	 * 
	 * @return paymentDueDate
	 */
	public String getPaymentDueDate() {
		return paymentDueDate;
	}
	
	/**
	 * 设置还款日
	 * 
	 * @param paymentDueDate
	 */
	public void setPaymentDueDate(String paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	
	/**
	 * 获取美元预借现金额度
	 * 
	 * @return cashUsaAdvanceLimit
	 */
	public double getCashUsaAdvanceLimit() {
		return cashUsaAdvanceLimit;
	}
	
	/**
	 * 设置美元预借现金额度
	 * 
	 * @param cashUsaAdvanceLimit
	 */
	public void setCashUsaAdvanceLimit(double cashUsaAdvanceLimit) {
		this.cashUsaAdvanceLimit = cashUsaAdvanceLimit;
	}
	
	/**
	 * 获取人民币预借现金额度
	 * 
	 * @return cashRmbAdvanceLimit
	 */
	public double getCashRmbAdvanceLimit() {
		return cashRmbAdvanceLimit;
	}
	
	/**
	 * 设置人民币预借现金额度
	 * 
	 * @param cashRmbAdvanceLimit
	 */
	public void setCashRmbAdvanceLimit(double cashRmbAdvanceLimit) {
		this.cashRmbAdvanceLimit = cashRmbAdvanceLimit;
	}
	
	/**
	 * 获取用户姓名
	 * 
	 * @return userName
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * 设置用户姓名
	 * 
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * 获取用户性别
	 * 
	 * @return userGender
	 */
	public String getUserGender() {
		return userGender;
	}
	
	/**
	 * 设置用户性别
	 * 
	 * @param userGender
	 */
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	
	/**
	 * 获取邮箱信息来源
	 * 
	 * @return infoSource
	 */
	public String getInfoSource() {
		return infoSource;
	}
	
	/**
	 * 设置邮箱信息来源
	 * 
	 * @param infoSource
	 */
	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}
	
	/**
	 * 获取服务端数据版本号
	 * 
	 * @return scVersion
	 */
	public long getScVersion() {
		return scVersion;
	}
	
	/**
	 * 设置服务端数据版本号
	 * 
	 * @param scVersion
	 */
	public void setScVersion(long scVersion) {
		this.scVersion = scVersion;
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
	
}
