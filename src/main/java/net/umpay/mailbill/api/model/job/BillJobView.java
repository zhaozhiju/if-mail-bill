package net.umpay.mailbill.api.model.job;


/**
 * 定时任务
 * 
 * @author admin
 *
 */
public class BillJobView {
	
	private Long id;				//用户的标示
	private Long accountId;			//用户的标示
	private int bankId;				//银行标示
	private String cardEndOfFour;	//卡号末四位
	private String userEmail;		//用户邮箱地址
	private String password;		//用户邮箱密码
	private int billDate;			//账单日
	private int billType;			//账单类型
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	
	public int getBankId() {
		return bankId;
	}
	public void setBankId(int bankId) {
		this.bankId = bankId;
	}
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public int getBillDate() {
		return billDate;
	}
	public void setBillDate(int billDate) {
		this.billDate = billDate;
	}
	public int getBillType() {
		return billType;
	}
	public void setBillType(int billType) {
		this.billType = billType;
	}
}
