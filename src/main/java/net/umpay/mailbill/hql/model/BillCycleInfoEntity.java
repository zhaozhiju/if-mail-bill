package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;
/**
 * 邮件账单周期表
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_CYCLE_INFO")
public class BillCycleInfoEntity extends IdEntity{
	
	private static final long serialVersionUID = -6928585428780787655L;
	private	String senderUrl;//用户发件人的邮箱
	private	String receiveAddUrl;//用户收件人的邮箱
	private	Date sentData;//用户收件人的时间
	private	String subject;//邮件主题
	private	String userName;//用户姓名
	private	String userGender;//用户性别
	private	String cardEndOfFour;//卡号末四位
	private	Integer bankId;//银行类型（银行主键id）
	private	String cardType;//银行卡品种（如：VISA。。。）
	private	Integer billType;//账单类型(1 月账单;  2 日账单;)
	private	Date billCycleBegin;//账单周期开始日期
	private	Date billCycleEnd;//账单周期结束日期
	private	Integer billDate;//账单日
	private	String accountOfDate;//账期账期（月账单:yyyymm 日账单:yyyymmdd）
	private	double rmbCreditLimit;//人民币信用额度
	private	double usaCreditLimit;//美元信用额度
	private	double newRmbBalance;//本期应还人民币总额
	private	double newUsaBalance;//本期应还美元总额
	private	double minRmbPayment;//本期最低人民币还款额
	private	double minUsaPayment;//本期最低美元还款额
	private	double pastDueAmount;//逾期还款额（目前只是浦发）
	private	Date paymentDueDate;//到期还款日
	private	double cashUsaAdvanceLimit;//美元预借现金额度
	private	double cashRmbAdvanceLimit;//人民币预借现金额度
	private	long usaIntegration;//美元可用积分余额
	private	long rmbIntegration;//人民币可用积分余额
	private	String oldHtmlUrl;//原邮件HTML访问地址
	private	String newHtmlUrl;//截取拼接后HTML访问地址
	private	String oldHtmlDFS;//DFS分布式文件原始邮件HTML访问地址
	private	String newHtmlDFS;//DFS分布式文件截取拼接后HTML访问地址
	private	String infoSource;//邮箱信息来源
	private	String detail;//信息描述
	private Integer isBill;//是否为账单（0：否；1：是）
	private Integer isPush = 0;//是否推送到app（0：否；1：是）默认是0
	private Integer bankBillType ;//银行账单的具体类型,与VO相对
	private Long accountId;	//用户账号
	private Long scVersion;	//服务端数据版本号

	@Column(name="SENT_DATA")
	public Date getSentData() {
		return sentData;
	}
	@Column(name="SUBJECT")
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public void setSentData(Date sentData) {
		this.sentData = sentData;
	}
	@Column(name="RECEIVE_ADD_URL")
	public String getReceiveAddUrl() {
		return receiveAddUrl;
	}
	public void setReceiveAddUrl(String receiveAddUrl) {
		this.receiveAddUrl = receiveAddUrl;
	}
	@Column(name="OLD_HTML_DFS")
	public String getOldHtmlDFS() {
		return oldHtmlDFS;
	}
	public void setOldHtmlDFS(String oldHtmlDFS) {
		this.oldHtmlDFS = oldHtmlDFS;
	}
	@Column(name="NEW_HTML_DFS")
	public String getNewHtmlDFS() {
		return newHtmlDFS;
	}
	public void setNewHtmlDFS(String newHtmlDFS) {
		this.newHtmlDFS = newHtmlDFS;
	}
	@Column(name="USER_NAME")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Column(name="CARD_END_OF_FOUR")
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	@Column(name="USER_GENDER")
	public String getUserGender() {
		return userGender;
	}
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	@Column(name="SENDER_URL")
	public String getSenderUrl() {
		return senderUrl;
	}
	public void setSenderUrl(String senderUrl) {
		this.senderUrl = senderUrl;
	}
	@Column(name="BANK_ID")
	public Integer getBankId() {
		return bankId;
	}
	public void setBankId(Integer bankId) {
		this.bankId = bankId;
	}
	@Column(name="CARD_TYPE")
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	@Column(name="BILL_TYPE")
	public Integer getBillType() {
		return billType;
	}
	public void setBillType(Integer billType) {
		this.billType = billType;
	}
	@Column(name="BILL_CYCLE_BEGIN")
	public Date getBillCycleBegin() {
		return billCycleBegin;
	}
	public void setBillCycleBegin(Date billCycleBegin) {
		this.billCycleBegin = billCycleBegin;
	}
	@Column(name="BILL_CYCLE_END")
	public Date getBillCycleEnd() {
		return billCycleEnd;
	}
	public void setBillCycleEnd(Date billCycleEnd) {
		this.billCycleEnd = billCycleEnd;
	}
	@Column(name="BILL_DATE")
	public Integer getBillDate() {
		return billDate;
	}
	public void setBillDate(Integer billDate) {
		this.billDate = billDate;
	}
	@Column(name="ACCOUNT_OF_DATE")
	public String getAccountOfDate() {
		return accountOfDate;
	}
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}
	@Column(name="RMB_CREDIT_LIMIT")
	public double getRmbCreditLimit() {
		return rmbCreditLimit;
	}
	public void setRmbCreditLimit(double rmbCreditLimit) {
		this.rmbCreditLimit = rmbCreditLimit;
	}
	@Column(name="USA_CREDIT_LIMIT")
	public double getUsaCreditLimit() {
		return usaCreditLimit;
	}
	public void setUsaCreditLimit(double usaCreditLimit) {
		this.usaCreditLimit = usaCreditLimit;
	}
	@Column(name="NEW_RMB_BALANCE")
	public double getNewRmbBalance() {
		return newRmbBalance;
	}
	public void setNewRmbBalance(double newRmbBalance) {
		this.newRmbBalance = newRmbBalance;
	}
	@Column(name="NEW_USA_BALANCE")
	public double getNewUsaBalance() {
		return newUsaBalance;
	}
	public void setNewUsaBalance(double newUsaBalance) {
		this.newUsaBalance = newUsaBalance;
	}
	@Column(name="MIN_RMB_PAYMENT")
	public double getMinRmbPayment() {
		return minRmbPayment;
	}
	public void setMinRmbPayment(double minRmbPayment) {
		this.minRmbPayment = minRmbPayment;
	}
	@Column(name="MIN_USA_PAYMENT")
	public double getMinUsaPayment() {
		return minUsaPayment;
	}
	public void setMinUsaPayment(double minUsaPayment) {
		this.minUsaPayment = minUsaPayment;
	}
	@Column(name="PAST_DUE_AMOUNT")
	public double getPastDueAmount() {
		return pastDueAmount;
	}
	public void setPastDueAmount(double pastDueAmount) {
		this.pastDueAmount = pastDueAmount;
	}
	@Column(name="PAYMENT_DUE_DATE")
	public Date getPaymentDueDate() {
		return paymentDueDate;
	}
	public void setPaymentDueDate(Date paymentDueDate) {
		this.paymentDueDate = paymentDueDate;
	}
	@Column(name="CASH_USA_ADVANCE_LIMIT")
	public double getCashUsaAdvanceLimit() {
		return cashUsaAdvanceLimit;
	}
	public void setCashUsaAdvanceLimit(double cashUsaAdvanceLimit) {
		this.cashUsaAdvanceLimit = cashUsaAdvanceLimit;
	}
	@Column(name="CASH_RMB_ADVANCE_LIMIT")
	public double getCashRmbAdvanceLimit() {
		return cashRmbAdvanceLimit;
	}
	public void setCashRmbAdvanceLimit(double cashRmbAdvanceLimit) {
		this.cashRmbAdvanceLimit = cashRmbAdvanceLimit;
	}
	@Column(name="USA_INTEGRATION")
	public long getUsaIntegration() {
		return usaIntegration;
	}
	public void setUsaIntegration(long usaIntegration) {
		this.usaIntegration = usaIntegration;
	}
	@Column(name="RMB_INTEGRATION")
	public long getRmbIntegration() {
		return rmbIntegration;
	}
	public void setRmbIntegration(long rmbIntegration) {
		this.rmbIntegration = rmbIntegration;
	}
	@Column(name="OLD_HTML_URL")
	public String getOldHtmlUrl() {
		return oldHtmlUrl;
	}
	public void setOldHtmlUrl(String oldHtmlUrl) {
		this.oldHtmlUrl = oldHtmlUrl;
	}
	@Column(name="NEW_HTML_URL")
	public String getNewHtmlUrl() {
		return newHtmlUrl;
	}
	public void setNewHtmlUrl(String newHtmlUrl) {
		this.newHtmlUrl = newHtmlUrl;
	}
	@Column(name="INFO_SOURCE")
	public String getInfoSource() {
		return infoSource;
	}
	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	@Column(name="IS_BILL")
	public Integer getIsBill() {
		return isBill;
	}
	public void setIsBill(Integer isBill) {
		this.isBill = isBill;
	}
	@Column(name="IS_PUSH")
	public Integer getIsPush() {
		return isPush;
	}
	public void setIsPush(Integer isPush) {
		this.isPush = isPush;
	}
	@Column(name="BANK_BILL_TYPE")
	public Integer getBankBillType() {
		return bankBillType;
	}
	public void setBankBillType(Integer bankBillType) {
		this.bankBillType = bankBillType;
	}
	@Column(name="ACCOUNT_ID")
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	@Column(name="SC_VERSION")
	public Long getScVersion() {
		return scVersion;
	}
	public void setScVersion(Long scVersion) {
		this.scVersion = scVersion;
	}
}
