package net.umpay.mailbill.api.model.viewpart;


/**
 * 周期表相对于VO拆开来的部分
 * @author admin
 *
 */
public class CycleBillDayView {

	private	String userName;//用户姓名
	private	String userGender;//用户性别
	private	String cardEndOfFour;//卡号末四位
	private	String cashRmbAdvanceLimit;//人民币取现额度
	private	String rmbCreditLimit;//人民币可用额度
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
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	public String getRmbCreditLimit() {
		return rmbCreditLimit;
	}
	public void setRmbCreditLimit(String rmbCreditLimit) {
		this.rmbCreditLimit = rmbCreditLimit;
	}
	public String getCashRmbAdvanceLimit() {
		return cashRmbAdvanceLimit;
	}
	public void setCashRmbAdvanceLimit(String cashRmbAdvanceLimit) {
		this.cashRmbAdvanceLimit = cashRmbAdvanceLimit;
	}

}
