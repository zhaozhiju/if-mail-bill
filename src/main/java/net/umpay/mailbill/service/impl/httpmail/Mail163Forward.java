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
 * 163邮箱添加转发规则
 * 
 * @version 1.0.0
 */
@Service
public class Mail163Forward implements IMailForward {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(Mail163Forward.class);
	@Autowired
	private HttpMailBy163 httpMailBy163;

	// 163邮箱登录链接
	private final String SESSION_INIT = "http://mail.163.com";
	private final String LOGIN_URL = "https://ssl.mail.163.com/entry/coremail/fcg/ntesdoor2?df=mail163_letter&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&product=mail163&net=t&style=-1&race=89_66_90_bj&uid=";
	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_163_TYPE;
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
		data.put("url2", "http://mail.163.com/errorpage/err_163.htm");
		data.put("savelogin", "0");
		data.put("username", mailUrl);
		data.put("password", password);
		lr = hc.post(LOGIN_URL, data, setHeader());// 执行登录

		if (!lr.getHtml().contains("http://mail.163.com/errorpage/err_163.htm")) {
			log.info("e_mail:{} http login success for forward", mailUrl);
			try {
				// 获取sid
				Document doc = Jsoup.parse(lr.getHtml());
				String sid = doc.select("script").html().split("=")[2];
				sid = sid.substring(0, sid.length() - 2);

				flag = httpMailBy163.forwardRules(data, lr, hc, sid, mailUrl,
						forwardMail);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			flag = false;
			log.info("e_mail:{} login failed, please check email or password", mailUrl);
		}

		return flag;
	}
	
	// ---------- private function ----------------
	private Header[] setHeader() {
        Header[] result = {
        		new BasicHeader("Host","ssl.mail.163.com"),
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","http://mail.163.com/"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }

}
