package net.umpay.mailbill.util.exception;

/**
 * 业务流程异常类
 * 
 * @author zhaozj 
 * @version 1.0.0
 */
public class MailBillException extends Exception {

	private static final long serialVersionUID = 1980019507208526746L;

	private Integer errorCode;

	public MailBillException() {
		super();
	}

	public MailBillException(String errormsg) {
		super(errormsg);

	}

	public MailBillException(String errormsg, Integer errorCode) {
		super(errormsg);
		this.errorCode = errorCode;
	}

	public MailBillException(String prompt, Throwable cause) {
		super(prompt, cause);
	}

	/**
	 * MailBillException
	 * 
	 * @param prompt
	 * @param cause
	 *            抛出原因
	 * @param errorCode
	 *            错误类型码
	 */
	public MailBillException(String prompt, Throwable cause, Integer errorCode) {
		super(prompt, cause);
		this.errorCode = errorCode;
	}

	public String getPrompt() {
		return this.getMessage();
	}

	public Integer getErrorCode() {
		return this.errorCode;
	}

}
