package net.umpay.mailbill.service.impl.httpmail;

import java.util.HashMap;
import java.util.Map;

import net.umpay.mailbill.api.httpmail.IMailForward;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.mail.httpmail.HttpClientHelper;
import net.umpay.mailbill.util.mail.httpmail.HttpResult;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * yeah.net邮箱添加转发规则
 */
@Service
public class MailYeahForward implements IMailForward {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(MailYeahForward.class);

	@Autowired
	private HttpMailByYeah httpMailByYeah;

	// yeah登录链接
	private final String SESSION_INIT = "http://mail.yeah.net";
	private final String LOGIN_URL = "https://mail.yeah.net/entry/cgi/ntesdoor?df=webmailyeah&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&verifycookie=1&product=mailyeah&style=-1&uid=";

	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_YEAH_TYPE;
	}
  	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addForwardRules(String mailUrl, String password, String forwardMail) throws MailBillException {
		boolean flag = false;
		HttpClientHelper hc = new HttpClientHelper(true);
		HttpResult lr = hc.get(SESSION_INIT);// 目的是得到 csrfToken 类似

		// 拼装登录信息
		Map<String, String> data = new HashMap<String, String>();
		data.put("username", mailUrl);
		data.put("savelogin", "0");
		data.put("url2", "http://mail.yeah.net/errorpage/err_yeah.htm");
		data.put("password", password);
		lr = hc.post(LOGIN_URL, data, setHeader());// 执行登录

		if (!lr.getHtml().contains(
				"http://mail.yeah.net/errorpage/err_yeah.htm")) {
			log.info("e_mail:{} http login success for forward", mailUrl);
			try {
				Document doc = Jsoup.parse(lr.getHtml());
				String sessionId = doc.select("script").html().split("=")[2];
				sessionId = sessionId.substring(0, sessionId.length() - 2);

				flag = httpMailByYeah.forwardRules(data, lr, hc, sessionId,
						mailUrl, forwardMail);

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			flag = false;
			log.info("e_mail:{} login failed, please check mail or password",
					mailUrl);
		}

		return flag;
	}

	// ----------- private function -------------
	private Header[] setHeader() {
        Header[] result = {
        		new BasicHeader("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Host","mail.yeah.net"),
                new BasicHeader("Referer","http://mail.yeah.net/"),
                new BasicHeader("Accept","text/html, application/xhtml+xml, */*")
                 
        };
        return result;
    }

}
