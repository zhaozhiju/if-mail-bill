package net.umpay.mailbill.api.httpmail;

import java.util.Map;

import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.util.exception.MailBillException;

/**
 * http方式抓取邮件和设置转发规则 
 * @version 1.0.0
 */
public interface IHttpMail {

	/**
	 * http抓取邮件
	 * 
	 * @param userUrl 			email地址	
	 * @param password			email密码
	 * @param forwardMail		转发至指定mail
	 * @param key				websoket连接的key值
	 * @param accountid_phoneId	日志accountid_phoneId
	 * @param logMap			存放日志信息的map
	 * @throws MailBillException
	 */
	public void httpScan(String userUrl, String password, String forwardMail, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap) throws MailBillException;
	
	/**
	 * 邮箱厂商
	 * @return String  {@link net.umpay.mailbill.util.constants.MailBillTypeConstants}
	 */
	public String httpScanType();
}
