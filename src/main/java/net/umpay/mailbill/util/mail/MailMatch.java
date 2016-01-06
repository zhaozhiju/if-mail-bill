package net.umpay.mailbill.util.mail;

import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import net.umpay.mailbill.api.resolve.ISystemNotSpportInfoService;
import net.umpay.mailbill.hql.model.SystemNotSpportInfoEntity;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * javaMail方式 --邮箱登录
 * 
 * <p>
 * 	javamail协议pop&imap
 * 	<li>1. pop3协议仅读取收件箱;</li>
 * 	<li>2. imap协议可以读取收件箱和其他自定义收件箱;</li>
 * </p>
 * 
 * @version 1.0.0
 */
@Service
public class MailMatch {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(MailMatch.class);

	@Autowired
	private ISystemNotSpportInfoService systemNotSpportInfoService;

	/**
	 * 根据邮箱账号进行相应的匹配
	 * 
	 * @param mailUrl
	 *            用户输入的邮箱地址
	 * @param password
	 *            用户输入的邮箱密码
	 * @return Store
	 * @throws MailBillException
	 */
	
	public Store match(String mailUrl, String password)
			throws MailBillException {
		// 校验mailUrl是否为非空及格式
		if (StringUtils.isNotBlank(mailUrl)) {
			if (mailUrl.indexOf("@") == -1) {
				log.error("mail:{} is format error", mailUrl);
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.MAIL_FORMAT_EXCEPTION_CODE,
						ErrorCodeContants.MAIL_FORMAT_EXCEPTION.getMsg(), log);
			} else {
				// do nothing
			}
		} else {
			log.error("mail url is null");
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.MAIL_URL_NULL_CODE,
					ErrorCodeContants.MAIL_URL_NULL.getMsg(), log);
		}

		String mailAddr_sub = mailUrl.substring(mailUrl.lastIndexOf("@"));
		Store store = null;

		// 适配策略
		if (mailAddr_sub.equals("@yinxinbao.cn")) {
			store = pop3Login("pop3.yinxinbao.cn", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@qq.com")) {
			store = pop3SslLogin("pop.qq.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@163.com")) {
			store = pop3Login("pop3.163.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@126.com")) {
			store = pop3Login("pop3.126.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@sina.com")) {
			store = pop3Login("pop.sina.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@hotmail.com")) {
			store = pop3SslLogin("pop3.live.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@gmail.com")) {
			store = pop3SslLogin("pop.gmail.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@sohu.com")) {
			store = pop3Login("pop3.sohu.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@tom.com")) {
			store = pop3Login("pop.tom.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@yeah.net")) {
			store = pop3Login("pop.yeah.net", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@21cn.com")) {
			store = pop3Login("pop.21cn.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@vip.qq.com")) {
			store = pop3SslLogin("pop.qq.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@139.com")) {
			store = pop3Login("pop.139.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@263.net")) {
			store = pop3Login("pop.263.net", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@sina.cn")) {
			store = pop3Login("pop.sina.cn", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@yahoo.com")) {
			store = pop3SslLogin("pop.mail.yahoo.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@foxmail.com")) {
			store = pop3SslLogin("pop.qq.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@189.cn")) {
			store = pop3Login("pop.189.cn", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@aliyun.com")) {
			store = pop3Login("pop3.aliyun.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@outlook.com")) {
			store = pop3SslLogin("pop3.live.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@vip.163.com")) {
			store = pop3Login("pop3.vip.163.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@vip.126.com")) {
			store = pop3Login("pop3.vip.126.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@vip.sohu.com")) {
			store = pop3Login("pop3.vip.sohu.com", mailUrl, password);
			return store;
		} else if (mailAddr_sub.equals("@188.com")) {
			store = pop3Login("pop3.188.com", mailUrl, password);
			return store;
		} else {
			log.error("mail:{} no match", mailUrl);
			SystemNotSpportInfoEntity findByEmailSuffix = systemNotSpportInfoService
					.findByEmailSuffix(mailAddr_sub);
			int bindingNumber = 0;
			if (findByEmailSuffix != null) {
				bindingNumber = findByEmailSuffix.getBindingNumber();
				bindingNumber = bindingNumber + 1;
				findByEmailSuffix.setBindingNumber(bindingNumber);
				findByEmailSuffix.setBindingDate(new Date());
				systemNotSpportInfoService.save(findByEmailSuffix);
			} else {
				SystemNotSpportInfoEntity entity = new SystemNotSpportInfoEntity();
				entity.setBindingDate(new Date());
				entity.setBindingNumber(bindingNumber++);
				entity.setEmailSuffix(mailAddr_sub);
				entity.setDetail(mailUrl);
				systemNotSpportInfoService.save(entity);
			}
			throw MailBillExceptionUtil.getWithLog(
					ErrorCodeContants.NOT_SUPPORT_EMAIL_CODE,
					ErrorCodeContants.NOT_SUPPORT_EMAIL.getMsg(), log);
		}
	}

	/**
	 * 采用pop3方式登陆
	 * 
	 * @param serverAddress
	 *            邮箱服务器地址
	 * @param mailAddr
	 *            用户输入的邮箱地址
	 * @param password
	 *            用户输入的邮箱密码
	 * @return Store
	 * @throws MessagingException
	 */
	
	public static Store pop3Login(String serverAddress, final String mailAddr,
			final String password) throws MailBillException {
		log.info("begin landing user mail:{}", mailAddr);
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "pop3"); // 协议
		props.setProperty("mail.pop3.port", "110"); // 端口
		props.setProperty("mail.pop3.host", serverAddress);// 服务器地址
		// 创建Session实例对象
		Session session = Session.getInstance(props);

		try {
			// 创建pop3协议的Store对象
			Store store = session.getStore("pop3");
			store.connect(mailAddr, password);
			boolean connected = store.isConnected();
			log.info("mail:{} \tis connected:{}", new Object[] { mailAddr,
					connected });
			if (!connected) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE,
						ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
			}
			return store;
		} catch (NoSuchProviderException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL_CODE,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL.getMsg(), log);
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.POP_NOT_OPEN_EXCEPTION_CODE,
					ErrorCodeContants.POP_NOT_OPEN_EXCEPTION.getMsg(), log);
		}
	}

	/**
	 * 采用imap方式登陆
	 * 
	 * @param serverAddress
	 *            邮箱服务器地址
	 * @param mailAddr
	 *            用户输入的邮箱地址
	 * @param password
	 *            用户输入的邮箱密码
	 * @return Store
	 * @throws MailBillException
	 */
	
	public static Store imapLogin(String serverAddress, final String mailAddr,
			final String password) throws MailBillException {
		// 准备连接服务器的会话信息
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.imap.port", "143");
		props.setProperty("mail.imap.host", serverAddress);// 服务器地址

		// 创建Session实例对象
		Session session = Session.getInstance(props);

		try {
			// 创建pop3协议的Store对象
			Store store = session.getStore("imap");
			store.connect(mailAddr, password);
			boolean connected = store.isConnected();
			log.info("mail:{} \tis connect:{}", new Object[] { mailAddr,
					connected });
			if (!connected) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE,
						ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
			}

			return store;
		} catch (NoSuchProviderException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL_CODE,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL.getMsg(), log);
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION_CODE,
					ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION.getMsg(), log);
		}
	}

	/**
	 * 采用pop3+ssl方式登陆
	 * 
	 * @param serverAddress
	 *            邮箱服务器地址
	 * @param mailAddr
	 *            用户输入的邮箱地址
	 * @param password
	 *            用户输入的邮箱密码
	 * @return Store
	 * @throws MailBillException
	 */
	@SuppressWarnings({ "restriction" })
	public static Store pop3SslLogin(String serverAddress,
			final String mailAddr, final String password)
			throws MailBillException {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		// 准备连接服务器的会话信息
		Properties props = new Properties();

		// pop协议 ssl加密
		props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY); // SSL加密传送用户名密码
		props.setProperty("mail.pop3.socketFactory.fallback", "false");
		props.setProperty("mail.store.protocol", "pop3");
		props.setProperty("mail.pop3.port", "995");

		// 服务器地址
		props.setProperty("mail.pop3.host", serverAddress);
		// 创建Session实例对象
		Session session = Session.getInstance(props);

		try {
			// 创建IMAP协议的Store对象
			Store store = session.getStore("pop3");
			store.connect(mailAddr, password);
			boolean connected = store.isConnected();
			log.info("mail:{} \tis connect:{}", new Object[] { mailAddr,
					connected });
			if (!connected) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE,
						ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
			}
			return store;
		} catch (NoSuchProviderException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL_CODE,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL.getMsg(), log);
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.POP_NOT_OPEN_EXCEPTION_CODE,
					ErrorCodeContants.POP_NOT_OPEN_EXCEPTION.getMsg(), log);
		}
	}

	/**
	 * 采用imap+ssl方式登陆
	 * 
	 * @param serverAddress
	 *            邮箱服务器地址
	 * @param mailAddr
	 *            用户输入的邮箱地址
	 * @param password
	 *            用户输入的邮箱密码
	 * @return Store
	 * @throws MailBillException
	 */
	@SuppressWarnings({ "restriction" })
	public static Store imapSslLogin(String serverAddress,
			final String mailAddr, final String password)
			throws MailBillException {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		// 准备连接服务器的会话信息
		Properties props = new Properties();

		// imap协议 ssl加密
		props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY); // SSL加密传送用户名密码
		props.setProperty("mail.imap.socketFactory.fallback", "false");
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.imap.port", "993");

		// 服务器地址
		props.setProperty("mail.imap.host", serverAddress);

		// 创建Session实例对象
		Session session = Session.getInstance(props);
		try {
			// 创建IMAP协议的Store对象
			Store store = session.getStore("imap");
			store.connect(mailAddr, password);
			boolean connected = store.isConnected();
			log.info("mail:{} \tis connect:{}", new Object[] { mailAddr,
					connected });
			if (!connected) {
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE,
						ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
			}
			return store;
		} catch (NoSuchProviderException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL_CODE,
					ErrorCodeContants.EMAIL_INVALID_PROTOCOL.getMsg(), log);
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION_CODE,
					ErrorCodeContants.IMAP_NOT_OPEN_EXCEPTION.getMsg(), log);
		}

	}

	public static void main(String[] args) throws Exception {
		// 24家第三方邮箱测试已通过
		// match("test@yinxinbao.cn", "123456");
		// match("576781101@qq.com","18701636117");
		// match("ycj7126168@163.com","18701636117");
		// match("ycj7126168@126.com","ycj4299359");
		// match("ycj7126168@sina.com","ycj4299359");
		// match("ycj7126168@hotmail.com","ycj4299359");
		// match("ycj7126168@gmail.com","ycj4299359");
		// match("ycj7126168@sohu.com","ycj4299359");
		// match("ycj7126168@tom.com","ycj4299359");
		// match("ycj7126168@yeah.net","ycj4299359");
		// match("ycj7978737@21cn.com","7978737");
		// match("yinxinbao@vip.qq.com","umpay@0827"); //收费
		// match("18701636117@139.com","ycj4299359");
		// match("xiaobaoer@263.net","umpay2014"); //收费
		// match("ycj7126168@sina.cn","ycj4299359");
		// match("ycj7126168@yahoo.com","Ycj4299359");
		// match("ycj7126168@foxmail.com","ycj4299359");
		// match("18701636117@189.cn","ycj4299359");
		// match("ycj7126168@aliyun.com","ycj4299359");
		// match("ycj7126168@outlook.com","ycj4299359");
		// match("yinxinbao@vip.163.com","umpayyxb"); //收费
		// match("yinxinbao@vip.126.com","umpayyxb"); //收费
		// match("yinxinbao@vip.sohu.com","umpayyxb"); //收费
		// match("yinxinbao@188.com","umpayyxb"); //收费
	}
}
