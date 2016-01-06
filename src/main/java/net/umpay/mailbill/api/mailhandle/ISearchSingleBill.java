package net.umpay.mailbill.api.mailhandle;

import java.util.List;

/**
 * 获取下载文件中缺失第二次下载的邮件账单
 * @author admin
 *
 */
@Deprecated
public interface ISearchSingleBill {

	/**
	 *  搜索下载文件中缺失第二次下载的邮件账单的路径
	 *  
	 * @return  返回下载文件中缺失第二次下载的邮件账单的文件路径列表
	 */
	public List<String> searchSingleFliesPath();
}
