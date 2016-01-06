package net.umpay.mailbill.api.mailhandle;

import java.util.List;

/**
 * 将只经过第一次下载的HTML，进行第二次下载
 * 
 * @author admin
 *
 */
@Deprecated
public interface ISecondDownload {

	/**
	 * 将只经过第一次下载的HTML，进行第二次下载
	 * 
	 * @param onceMailsList   只经过第一次下载的HTML邮件的路径列表
	 * @return 下载成功的个数
	 */
	public int downloadSecond(List<String> onceMailsList);
}
