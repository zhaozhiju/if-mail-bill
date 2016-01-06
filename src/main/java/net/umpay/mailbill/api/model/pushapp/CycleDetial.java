package net.umpay.mailbill.api.model.pushapp;


/**
 * 账期表详情VO
 * 
 */
public class CycleDetial {

	private long id;				// 账单周期表主键
	private String accountOfDate;	// 账期
	private String cardEndOfFour;	// 卡号后四位
	private String billCycle;		// 账单周期
	private String paymentDueDate;	// 到期还款日
	private double newRmbBalance;	// 本期应还人民币总额
	private double newUsaBalance;	// 本期应还美元总额
	private double minRmbPayment;	// 本期最低人民币还款额
	private double minUsaPayment;	// 本期最低美元还款额
	private String infoSource;		// 邮箱信息来源
	private String subject;			// 邮件主题
	
	/**
	 * 获取账单周期表主键
	 * 
	 * @return id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * 设置账单周期表主键
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * 获取账期
	 * 
	 * @return String
	 */
	public String getAccountOfDate() {
		return accountOfDate;
	}
	
	/**
	 * 设置账期
	 * 
	 * @param accountOfDate
	 */
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}
	
	/**
	 * 获取卡号后四位
	 * 
	 * @return cardEndOfFour
	 */
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	
	/**
	 * 设置卡号后四位
	 * 
	 * @param cardEndOfFour
	 */
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	
	/**
	 * 获取账单周期
	 * 
	 * @return billCycle
	 */
	public String getBillCycle() {
		return billCycle;
	}
	
	/**
	 * 设置账单周期
	 * 
	 * @param billCycle
	 */
	public void setBillCycle(String billCycle) {
		this.billCycle = billCycle;
	}
	
	/**
	 * 获取还款日
	 * 
	 * @return String
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
	 * 获取本期应还人民币总额
	 * 
	 * @return newRmbBalance
	 */
	public double getNewRmbBalance() {
		return newRmbBalance;
	}
	
	/**
	 * 设置本期应还人民币总额
	 * 
	 * @param newRmbBalance
	 */
	public void setNewRmbBalance(double newRmbBalance) {
		this.newRmbBalance = newRmbBalance;
	}
	
	/**
	 * 获取本期应还美元总额
	 * 
	 * @return newUsaBalance
	 */
	public double getNewUsaBalance() {
		return newUsaBalance;
	}
	
	/**
	 * 设置本期应还美元总额
	 * 
	 * @param newUsaBalance
	 */
	public void setNewUsaBalance(double newUsaBalance) {
		this.newUsaBalance = newUsaBalance;
	}
	
	/**
	 * 获取本期最低人民币还款额
	 * 
	 * @return minRmbPayment
	 */
	public double getMinRmbPayment() {
		return minRmbPayment;
	}
	
	/**
	 * 设置本期最低人民币还款额
	 * 
	 * @param minRmbPayment
	 */
	public void setMinRmbPayment(double minRmbPayment) {
		this.minRmbPayment = minRmbPayment;
	}
	
	/**
	 * 获取本期最低美元还款额
	 * 
	 * @return minUsaPayment
	 */
	public double getMinUsaPayment() {
		return minUsaPayment;
	}
	
	/**
	 * 设置本期最低美元还款额
	 * 
	 * @param minUsaPayment
	 */
	public void setMinUsaPayment(double minUsaPayment) {
		this.minUsaPayment = minUsaPayment;
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
	 * 获取邮件主题
	 * 
	 * @return subject
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * 设置邮件主题
	 * 
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
