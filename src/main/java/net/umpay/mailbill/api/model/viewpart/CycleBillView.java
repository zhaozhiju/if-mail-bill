package net.umpay.mailbill.api.model.viewpart;


/**
 * 周期表相对于VO拆开来的部分
 */
public class CycleBillView {

	private	String billDate;//账单日
	private	String rmbCreditLimit;//人民币信用额度
	private	String paymentDueDate;//到期还款日
	private	String newRmbBalance;//本期应还人民币总额
	private	String newUsaBalance;//本期应还美元总额
	private String minRmbPayment;//	本期账单最低还款额 、rmb
	private	String cardType;//银行卡品种（如：VISA。。。）
	private	String accountOfDate;//账期（月账单:yyyymm 日账单:yyyymmdd）
	private	String usaCreditLimit;//美元信用额度
	private	String minUsaPayment;//本期最低美元还款额
	private	String pastDueAmount;//逾期还款额（目前只是浦发）
	private	String cashUsaAdvanceLimit;//美元预借现金额度
	private	String cashRmbAdvanceLimit;//人民币预借现金额度
	private	String usaIntegration;//美元可用积分余额
	private	String rmbIntegration;//人民币可用积分余额
	private	String oldHtmlUrl;//原邮件HTML访问地址
	private	String newHtmlUrl;//截取拼接后HTML访问地址
	private	String userName;//用户姓名
	private	String userGender;//用户性别
	private	String cardEndOfFour;//卡号末四位

	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserGender() {
		return userGender;
	}
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	public String getBillDate() {
		return billDate;
	}
	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}
	public String getRmbCreditLimit() {
		return rmbCreditLimit;
	}
	public void setRmbCreditLimit(String rmbCreditLimit) {
		this.rmbCreditLimit = rmbCreditLimit;
	}
	public String getPaymentDueDate() {
		return paymentDueDate;
	}
	public void setPaymentDueDate(String paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	public String getNewRmbBalance() {
		return newRmbBalance;
	}
	public void setNewRmbBalance(String newRmbBalance) {
		this.newRmbBalance = newRmbBalance;
	}
	public String getNewUsaBalance() {
		return newUsaBalance;
	}
	public void setNewUsaBalance(String newUsaBalance) {
		this.newUsaBalance = newUsaBalance;
	}
	public String getMinRmbPayment() {
		return minRmbPayment;
	}
	public void setMinRmbPayment(String minRmbPayment) {
		this.minRmbPayment = minRmbPayment;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getAccountOfDate() {
		return accountOfDate;
	}
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}
	public String getUsaCreditLimit() {
		return usaCreditLimit;
	}
	public void setUsaCreditLimit(String usaCreditLimit) {
		this.usaCreditLimit = usaCreditLimit;
	}
	public String getMinUsaPayment() {
		return minUsaPayment;
	}
	public void setMinUsaPayment(String minUsaPayment) {
		this.minUsaPayment = minUsaPayment;
	}
	public String getPastDueAmount() {
		return pastDueAmount;
	}
	public void setPastDueAmount(String pastDueAmount) {
		this.pastDueAmount = pastDueAmount;
	}
	public String getCashUsaAdvanceLimit() {
		return cashUsaAdvanceLimit;
	}
	public void setCashUsaAdvanceLimit(String cashUsaAdvanceLimit) {
		this.cashUsaAdvanceLimit = cashUsaAdvanceLimit;
	}
	public String getCashRmbAdvanceLimit() {
		return cashRmbAdvanceLimit;
	}
	public void setCashRmbAdvanceLimit(String cashRmbAdvanceLimit) {
		this.cashRmbAdvanceLimit = cashRmbAdvanceLimit;
	}
	public String getUsaIntegration() {
		return usaIntegration;
	}
	public void setUsaIntegration(String usaIntegration) {
		this.usaIntegration = usaIntegration;
	}
	public String getRmbIntegration() {
		return rmbIntegration;
	}
	public void setRmbIntegration(String rmbIntegration) {
		this.rmbIntegration = rmbIntegration;
	}
	public String getOldHtmlUrl() {
		return oldHtmlUrl;
	}
	public void setOldHtmlUrl(String oldHtmlUrl) {
		this.oldHtmlUrl = oldHtmlUrl;
	}
	public String getNewHtmlUrl() {
		return newHtmlUrl;
	}
	public void setNewHtmlUrl(String newHtmlUrl) {
		this.newHtmlUrl = newHtmlUrl;
	}
	
}
