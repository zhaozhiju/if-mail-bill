package net.umpay.mailbill.hql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OptimisticLockType;

/**
 * 操作日志信息详情
 * @author admin
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true, selectBeforeUpdate = true, optimisticLock = OptimisticLockType.VERSION)
@Table(name="T_MAIL_BILL_LOG")
public class BillLogEntity extends IdEntity{

	private static final long serialVersionUID = -5677481016515303341L;
	
	private String emailUrl;		//邮箱地址
	private String phoneId;			//手机唯一标示
	private Date loginTime;			//登陆邮箱的开始时间
	private Date logoutTime;		//推送完数据的时间
	private long accountId;			//用户标示
	private int logOn;				//登陆是否成功（1：成功；2：失败）
	private int filtersNumber;		//过滤到疑似账单邮件的数目
	private int forwardResults;		//转发规则添加是否成功（1：成功；2：失败）
	private int downloadNumber;		//下载数目
	private int analysisNumber;		//解析数目
	private int exceptionNumber;	//报出异常的数目
	
	@Column(name="EMAIL_URL")
	public String getEmailUrl() {
		return emailUrl;
	}
	public void setEmailUrl(String emailUrl) {
		this.emailUrl = emailUrl;
	}
	@Column(name="PHONE_ID")
	public String getPhoneId() {
		return phoneId;
	}
	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}
	@Column(name="LOGIN_TIME")
	public Date getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
	@Column(name="LOGOUT_TIME")
	public Date getLogoutTime() {
		return logoutTime;
	}
	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}
	@Column(name="ACCOUNT_ID")
	public long getAccountId() {
		return accountId;
	}
	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}
	@Column(name="LOG_ON")
	public int getLogOn() {
		return logOn;
	}
	public void setLogOn(int logOn) {
		this.logOn = logOn;
	}
	@Column(name="FILTERS_NUMBER")
	public int getFiltersNumber() {
		return filtersNumber;
	}
	public void setFiltersNumber(int filtersNumber) {
		this.filtersNumber = filtersNumber;
	}
	@Column(name="FORWARD_RESULTS")
	public int getForwardResults() {
		return forwardResults;
	}
	public void setForwardResults(int forwardResults) {
		this.forwardResults = forwardResults;
	}
	@Column(name="DOWNLOAD_NUMBER")
	public int getDownloadNumber() {
		return downloadNumber;
	}
	public void setDownloadNumber(int downloadNumber) {
		this.downloadNumber = downloadNumber;
	}
	@Column(name="ANALYSIS_NUMBER")
	public int getAnalysisNumber() {
		return analysisNumber;
	}
	public void setAnalysisNumber(int analysisNumber) {
		this.analysisNumber = analysisNumber;
	}
	@Column(name="EXCEPTION_NUMBER")
	public int getExceptionNumber() {
		return exceptionNumber;
	}
	public void setExceptionNumber(int exceptionNumber) {
		this.exceptionNumber = exceptionNumber;
	}

	
}
