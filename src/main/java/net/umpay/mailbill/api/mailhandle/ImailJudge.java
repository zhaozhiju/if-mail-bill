package net.umpay.mailbill.api.mailhandle;

import java.util.List;

/**
 * 判断过滤到的邮件是否为账单邮件
 *
 */
public interface ImailJudge {

	/**
	 *  判断过滤到的邮件是否为账单邮件
	 *  @param parse 初步过滤后的邮件内容
	 *  @param subject 邮件主题
	 *  @return boolean
	 */
	public boolean Judge(List<String> parse, String subject);
}
