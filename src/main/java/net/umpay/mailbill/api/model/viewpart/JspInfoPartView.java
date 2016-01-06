package net.umpay.mailbill.api.model.viewpart;


/**
 * 主列表页
 * @author admin
 *
 */
public class JspInfoPartView {

	private Long id;
	private String oldHtml;
	private String oldDFS;
	private String newHtml;
	private String newDFS;
	private String cardEndOfFour;
	private int isBill;
	private int bankBillType;
	private String accountOfDate;
	private int number;
	
	public String getOldDFS() {
		return oldDFS;
	}
	public void setOldDFS(String oldDFS) {
		this.oldDFS = oldDFS;
	}
	public String getNewDFS() {
		return newDFS;
	}
	public void setNewDFS(String newDFS) {
		this.newDFS = newDFS;
	}
	public String getCardEndOfFour() {
		return cardEndOfFour;
	}
	public void setCardEndOfFour(String cardEndOfFour) {
		this.cardEndOfFour = cardEndOfFour;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOldHtml() {
		return oldHtml;
	}
	public void setOldHtml(String oldHtml) {
		this.oldHtml = oldHtml;
	}
	public String getNewHtml() {
		return newHtml;
	}
	public void setNewHtml(String newHtml) {
		this.newHtml = newHtml;
	}
	public int getBankBillType() {
		return bankBillType;
	}
	public void setBankBillType(int bankBillType) {
		this.bankBillType = bankBillType;
	}
	public int getIsBill() {
		return isBill;
	}
	public void setIsBill(int isBill) {
		this.isBill = isBill;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getAccountOfDate() {
		return accountOfDate;
	}
	public void setAccountOfDate(String accountOfDate) {
		this.accountOfDate = accountOfDate;
	}
	
}
