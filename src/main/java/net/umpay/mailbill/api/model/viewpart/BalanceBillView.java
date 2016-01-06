package net.umpay.mailbill.api.model.viewpart;

/**
 * 本期账单金额汇总信息
 * 
 * @author admin
 *
 */
public class BalanceBillView {

	private double balance;// 上期账单金额
	private double newCharges;//'本期账单金额
	private double payment;//上期还款金额'
	private double adjustment;//'本期调整金额'
	private double interest;//'循环利息
	private double newBalance;//'本期应缴余额
	private String currencyType;//币种
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public double getNewCharges() {
		return newCharges;
	}
	public void setNewCharges(double newCharges) {
		this.newCharges = newCharges;
	}
	public double getPayment() {
		return payment;
	}
	public void setPayment(double payment) {
		this.payment = payment;
	}
	public double getAdjustment() {
		return adjustment;
	}
	public void setAdjustment(double adjustment) {
		this.adjustment = adjustment;
	}
	public double getInterest() {
		return interest;
	}
	public void setInterest(double interest) {
		this.interest = interest;
	}
	public double getNewBalance() {
		return newBalance;
	}
	public void setNewBalance(double newBalance) {
		this.newBalance = newBalance;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	
}
