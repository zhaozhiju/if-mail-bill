package net.umpay.mailbill.util.mail.httpmail;

import java.util.List;

import net.umpay.mailbill.api.httpmail.IMailForward;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.mail.MailAnalyze;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 邮箱设置转发规则工厂
 * 
 * @version 1.0.0
 */
@Service
public class MailForwardFactory {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(MailForwardFactory.class);
	@Autowired
	private MailAnalyze mailAnalyze;
	// spring 注入
	private List<IMailForward> httpForwardList;
	public List<IMailForward> getHttpForwardList() {
		return httpForwardList;
	}

	public void setHttpForwardList(List<IMailForward> httpForwardList) {
		this.httpForwardList = httpForwardList;
	}

	/**
	 * 转发规则任务分发
	 * 
	 * @param mailUrl
	 *            mail地址
	 * @param password
	 *            mail密码
	 * @return bolean true 成功; false 失败;
	 * @throws MailBillException
	 */
	public boolean getMailForwardFactory(String mailUrl, String password)
			throws MailBillException {
		boolean forward_flag = false;
		// TODO 每个邮箱生成对应的自有邮箱，作为转发邮箱
		String forWardMail = "zzj99@126.com";
		String scanType = mailAnalyze.getHttpScanType(mailUrl);
		if (CollectionUtils.isNotEmpty(httpForwardList)) {
			for (IMailForward iMailForward : httpForwardList) {
				if (iMailForward.httpScanType().equals(scanType)) {
					log.info("e_mail:{} add forward rules\tforward_mail:{}", new Object[] { mailUrl, forWardMail});
					forward_flag = iMailForward.addForwardRules(mailUrl, password, forWardMail);
					break;
				}
			}
		} else {
			log.error("httpForwardList is null");
		}

		return forward_flag;
	}
	
}
