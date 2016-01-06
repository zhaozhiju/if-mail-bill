package net.umpay.mailbill.api.model.job;

import java.util.Date;

/**
 * 任务临时表
 * 
 * @author admin
 *
 */
public class BillJobTempView {
	
	private Long id;				//用户的主键
	private Long acountId;			//用户的标示
	private int bankId;				//银行标示
	private String cardEndOfFour;	//卡号末四位
	private String userEmail;		//用户邮箱地址
	private String password;		//密码
	private int billDate;			//账单日
	private int billType;			//账单类型
	private Date billStartDate;		//账单搜索的开始之间
	private Date billEndDate;		//搜索账单的最新时间
	private int isVaild;			//搜索账单是否有效
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAcountId() {
		return acountId;
	}
	public void setAcountId(Long acountId) {
		this.acountId = acountId;
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
	public Date getBillStartDate() {
		return billStartDate;
	}
	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
	public Date getBillEndDate() {
		return billEndDate;
	}
	public void setBillEndDate(Date billEndDate) {
		this.billEndDate = billEndDate;
	}
	public int getIsVaild() {
		return isVaild;
	}
	public void setIsVaild(int isVaild) {
		this.isVaild = isVaild;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
