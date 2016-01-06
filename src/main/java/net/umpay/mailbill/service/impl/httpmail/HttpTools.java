package net.umpay.mailbill.service.impl.httpmail;

import net.umpay.mailbill.util.constants.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http方式抓取邮件工具类
 */
public class HttpTools {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpTools.class);
	/**
	 * 根据关键字过滤邮件主题
	 * 
	 * @param subject 邮件主题
	 * @return boolean 适配结果  true 匹配上; false 未匹配上;
	 */
	public boolean httpSearch(String subject){
		log.info("action:{} \tsubject:{}", new Object[]{"httpSearch(String )", subject});
		String querykey = Constants.QUERYKEY;
		String[] querykeyArray = querykey.split("，");
		int length = querykeyArray.length;
		for(int i = 0; i < length; i++){
			if(subject.contains(querykeyArray[i])){
				return true;
			}
		}
		return false;
	}

}
