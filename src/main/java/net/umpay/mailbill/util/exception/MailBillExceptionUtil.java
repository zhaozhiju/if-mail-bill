package net.umpay.mailbill.util.exception;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * 异常工具类
 * 
 * @author zhaozj
 * @version 1.0.0
 */
public class MailBillExceptionUtil {

	/**
	 * 默认的错误码,执行失败
	 */
	public static final ErrorCodeContants default_error = ErrorCodeContants.UNKNOWN_ERROR;

	/**
	 * 记录异常信息 
	 * 不要用异常来做业务流程
	 * 
	 * @param errorMessage
	 *            附加错误信息
	 * @param e
	 *            异常
	 * @param errorCode
	 *            错误特征码
	 * @param logger
	 *            异常发生的类的logger
	 * @return MailBillException
	 */
	public static MailBillException getWithLog(Throwable e, Integer errorCode,
			String errorMessage, Logger logger) {
		if (errorCode == null) {
			errorCode = default_error.getCode();
		}
		MailBillException mbe = new MailBillException(errorMessage, e,
				errorCode);
		if (errorCode != null && StringUtils.isNotBlank(errorMessage)) {
			logger.error("errorCode:"+errorCode + "\terrorMessage:" + errorMessage, mbe);
		} else {
			logger.error(errorMessage, mbe);
		}

		return mbe;
	}

	/**
	 * 记录异常信息 
	 * 不要用异常来做业务流程
	 * 
	 * @param errorMessage
	 *            附加错误信息
	 * @param errorCode
	 *            错误特征码
	 * @param logger
	 *            异常发生的类的logger
	 * @return MailBillException
	 */
	public static MailBillException getWithLog(Integer errorCode,
			String errorMessage, Logger logger) {
		MailBillException mbe = new MailBillException(errorMessage, errorCode);
		if (errorCode != null && StringUtils.isNotBlank(errorMessage)) {
			logger.error("errorCode:"+errorCode + "\terrorMessage:" + errorMessage, mbe);
		} else {
			logger.error(errorMessage, mbe);
		}

		return mbe;
	}
}
