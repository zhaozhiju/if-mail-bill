package net.umpay.mailbill.util.constants;

import java.util.Map;

import net.umpay.mailbill.util.string.ReadProperty;

/**
 * 定义系统中的常量
 * @author zhaozj
 * add on 2014/05/13
 */
public class Constants {
	
	/***** 银行加密秘钥 ******/
	public static final String PASSWORDKEY = "123456789009876543211234";
	// 字符集编码
	public static final String CHARSET = "UTF-8";
	public static final String MAIL_FREQUENCY_SECONDS = "conf/callMails.properties";
	public static String PROPERTY_READ_PATH = "conf/queryKey.properties";
	public static String BANK_READ_PATH = "conf/bankMailAddress.properties";
	public static String FILE_READ_PATH = "conf/serverHtmlPath.properties";
	public static String SYSTEM_PATH = "conf/system.properties";
	
	public static final int QQ_MAIL_FREQUENCY_SECONDS = Integer.parseInt(getValue(MAIL_FREQUENCY_SECONDS, "qqMailFrequencySeconds"));
	public static final String HTTP_FETCH_MAIL = getValue(MAIL_FREQUENCY_SECONDS, "httpFetch");
	public static final String FILE_PATH = getValue(FILE_READ_PATH, "serverHtmlPath");
	public static final String QUERYKEY = getValue(PROPERTY_READ_PATH, "queryKey"); // 搜索关键字
	public static final String FORWARD_KEY = getValue(PROPERTY_READ_PATH, "forward_key"); // 转发关键字
	public static final String RULE_NAME = getValue(PROPERTY_READ_PATH, "rule_name"); // 转发规则名称
	public static final String BANK_MAIL_ADDRESS_ALL = getValue(BANK_READ_PATH,"bankMailAddress");
	public static final String DOMAIN_NAME_PATH = getValue(FILE_READ_PATH,"domainPath");
	public static final String DFS_GROUP = getValue(FILE_READ_PATH,"group");
	public static final String JOB_DAY = getValue(FILE_READ_PATH,"jobDay");
	//项目加载需要运行的属性
	public static final String TRANSPORT_HTTP_PORT = getValue(SYSTEM_PATH, "transport.http.port");
	public static final String TRANSPORT_HTTP_PROJECT_NAME = getValue(SYSTEM_PATH, "transport.http.project.name");
	public static final String TRANSPORT_HTTP_THREADS_MIN = getValue(SYSTEM_PATH, "transport.http.threads.min");
	public static final String TRANSPORT_HTTP_THREADS_MAX = getValue(SYSTEM_PATH, "transport.http.threads.max");
	public static final String TRANSPORT_HTTP_QUEUE_LIMIT = getValue(SYSTEM_PATH, "transport.http.queue.limit");
	// 用户绑定邮箱数量的上限阀值
	public static final String ACCOUNT_BINDING_MAIL_LIMIT = getValue(SYSTEM_PATH, "account.binding.mail.num");
	// 抓取邮件的方式
	public static final String HTTP_FETCH = "http_fetch";
	public static final String JAVAMAIL_FETCH = "javamail_fetch";
	
	
	private static String getValue(String path, String key) {
		Map<String, String> readMap = ReadProperty.readProperties(path);
		for (String str : readMap.keySet()){
			if (str.equals(key)){
				return readMap.get(str);
			}
		}
		return null;
	}
}
