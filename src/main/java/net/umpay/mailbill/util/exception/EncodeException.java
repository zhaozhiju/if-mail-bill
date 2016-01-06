package net.umpay.mailbill.util.exception;

/**
 * 编码异常类 
 */
public class EncodeException extends Exception {

	private static final long serialVersionUID = -398695049811404846L;

	private static ErrorCodeContants FAULT = ErrorCodeContants.ENCODE_EXCEPTION;

	private Object input;

	public EncodeException(){
		super(FAULT.getMsg());
	}
	
	public EncodeException(Object input) {
		super(FAULT.getMsg());
		this.input = input;
	}

	public Object getInputMessage() {
		return input;
	}

	public ErrorCodeContants makeFault() {
		return FAULT;
	}

}
