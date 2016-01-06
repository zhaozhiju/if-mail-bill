package net.umpay.mailbill.service.impl.httpmail;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.umpay.mailbill.api.httpmail.IMailForward;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.mail.httpmail.HttpClientHelper;
import net.umpay.mailbill.util.mail.httpmail.HttpResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 139邮箱设置转发规则
 * 
 * @version 1.0.0
 */
@Service
public class Mail139Forward implements IMailForward {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(Mail139Forward.class);
	@Autowired
	private HttpMailBy139 httpMailBy139;

	// 登录相关的链接
	private final String SESSION_INIT = "http://mail.10086.cn/";
	// 邮箱退出登录链接
	private final String LOGOUT_MAIL_URL = "http://mail.10086.cn/login/Logout.aspx?sid={0}&redirect=http://mail.10086.cn/logout.htm?code=6_600";

	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_139_TYPE;
	}
  	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addForwardRules(String mailUrl, String password,
			String forwardMail) throws MailBillException {
		boolean flag = false;
		// [1]. 登录邮箱
		HttpClientHelper hc = new HttpClientHelper(true);
		HttpResult lr = hc.get(SESSION_INIT);// 目的是得到 csrfToken 类似
		Map<String, String> paremeters = new HashMap<String, String>();
		StringBuffer sbStr = new StringBuffer();
		lr = httpMailBy139.mailLogin(hc, lr, mailUrl, password,"", paremeters);
		String cguid = paremeters.get("cguid");
		String sid = hc.getCookie("Os_SSo_Sid", "mail.10086.cn");
		if (null != sid) {
			log.info("e_mail:{} http login success for forward", mailUrl);
			// [2]. 设置转发规则
			flag =httpMailBy139.setMailFilters(hc, lr, sid, cguid, sbStr, forwardMail, mailUrl);
			// [3]. 退出邮箱
			lr = hc.get(MessageFormat.format(LOGOUT_MAIL_URL, sid),
					httpMailBy139.set139LogoutMail(sid, cguid));
			String string = lr.getHtml().length() > 0 ? "退出成功" : "退出失败";
			log.info("e_mail:{} \t http logout:{}", new Object[] { mailUrl, string });
		} else {
			flag = false;
			log.info("e_mail:{} login failed, please check your email user name and password", mailUrl);
		}

		return flag;
	}

}
