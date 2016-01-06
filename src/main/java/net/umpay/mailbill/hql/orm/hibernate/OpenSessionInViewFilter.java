package net.umpay.mailbill.hql.orm.hibernate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * 缩窄OpenSessionInViewFilter的过滤范围,请求css、js、图片等静态内容时不创建/关闭Hibernate Session连接.
 * 
 * 在web.xml中可配置excludeSuffixs参数,多个过滤文件后缀名以','分割.
 * 
 * 
 */
public class OpenSessionInViewFilter extends org.springframework.orm.hibernate3.support.OpenSessionInViewFilter {

	public static final String EXCLUDE_SUFFIXS_NAME = "excludeSuffixs";

	private static final String[] DEFAULT_EXCLUDE_SUFFIXS = { ".js", ".css", ".jpg", ".gif" };

	private String[] excludeSuffixs = DEFAULT_EXCLUDE_SUFFIXS;

	/**
	 * 重载过滤控制函数,忽略特定后缀名的请求.
	 */
	@Override
	protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();

		for (String suffix : excludeSuffixs) {
			if (path.endsWith(suffix))
				return true;
		}

		return false;
	}

	/**
	 * 初始化excludeSuffixs参数.
	 */
	@Override
	protected void initFilterBean() throws ServletException {

		String excludeSuffixStr = getFilterConfig().getInitParameter(EXCLUDE_SUFFIXS_NAME);

		if (StringUtils.isNotBlank(excludeSuffixStr)) {
			excludeSuffixs = excludeSuffixStr.split(",");
			//处理匹配字符串为".后缀名"
			for (int i = 0; i < excludeSuffixs.length; i++) {
				excludeSuffixs[i] = "." + excludeSuffixs[i];
			}
		}
	}
}
