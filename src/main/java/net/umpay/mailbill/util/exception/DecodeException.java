package net.umpay.mailbill.util.exception;

/**
 * 解码异常类
 */
public class DecodeException extends Exception {

	private static final long serialVersionUID = 6786997181595246796L;

	private Object input;

	private static ErrorCodeContants FAULT = ErrorCodeContants.DECODE_EXCEPTION;

	public DecodeException(){
		super(FAULT.getMsg());
	}
	
	public DecodeException(Object input) {
		super(FAULT.getMsg());
		this.input = input;
	}

	public Object getInputMessage() {
		return input;
	}
	
	public ErrorCodeContants makeFault( ) {
		return FAULT;
	}

}
