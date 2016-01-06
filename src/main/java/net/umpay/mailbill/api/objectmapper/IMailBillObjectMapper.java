/**
 * 
 */
package net.umpay.mailbill.api.objectmapper;

import net.umpay.mailbill.util.exception.MailBillException;

/**
 * 对象转换为json
 * @version 1.0.0
 */
public interface IMailBillObjectMapper {

	/**
	 * 将object对象转化为JSON串
	 * @param object
	 * @return
	 * @throws ForumException
	 */
	public String writeValueAsString(Object object) throws MailBillException;
}
