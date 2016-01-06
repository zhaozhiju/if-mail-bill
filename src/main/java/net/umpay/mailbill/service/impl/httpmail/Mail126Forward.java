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
 * 126邮箱添加转发规则
 */
@Service
public class Mail126Forward implements IMailForward {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(Mail126Forward.class);

	@Autowired
	private HttpMailBy126 httpMailBy126;

	// 126邮箱登录链接
	private final String SESSION_INIT = "http://mail.126.com";
	private final String LOGIN_URL = "https://mail.126.com/entry/cgi/ntesdoor?&df=mail126_letter&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&product=mail126&verifycookie=-1&net=failed&style=-1&race=-2_-2_-2_db&uid=";
	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_126_TYPE;
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
		data.put("url2", "http://mail.126.com/errorpage/err_126.htm");
		data.put("savelogin", "0");
		data.put("username", mailUrl);
		data.put("password", password);
		lr = hc.post(LOGIN_URL, data, setHeader());// 执行登录

		if (!lr.getHtml()
				.contains("http://mail.126.com/errorpage/err_126.htm?")) { // 判断是否登陆成功
			log.info("e_mail:{} http login success for forward", mailUrl);
			try {
				Document doc = Jsoup.parse(lr.getHtml());
				String sessionId = doc.select("script").html().split("=")[2];
				sessionId = sessionId.substring(0, sessionId.length() - 2);
				flag = httpMailBy126.forwardRules(data, lr, hc, sessionId,
						mailUrl, forwardMail);
			} catch (Exception e) {
				//TODO 未知异常，待回忆后再定义---晨杰快想
				log.error(e.getMessage(), e);
			}
		} else {
			log.info("e_mail:{} login failed, please check email or password",
					mailUrl);
			flag = false;
		}

		return flag;
	}

	// ----------   private function ----------
	private Header[] setHeader() {
        Header[] result = {
                new BasicHeader("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Cache-Control","no-cache"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded"),
                new BasicHeader("Host","ssl.mail.126.com"),
                new BasicHeader("Referer","http://mail.126.com/"),
                new BasicHeader("Accept","text/html, application/xhtml+xml, */*")
                 
        };
        return result;
    }
}
