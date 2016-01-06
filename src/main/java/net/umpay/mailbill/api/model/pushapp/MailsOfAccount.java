package net.umpay.mailbill.api.model.pushapp;

import java.util.Set;

/**
 * 用户账号下有效的列表vo
 *  
 * @author zhaozj
 */
public class MailsOfAccount {
	private String dataType; // 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	private Set<String> mailArr; // 邮箱列表

	/**
	 * 获取数据类型 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	 * 
	 * @return String
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * 设置数据类型 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	 * 
	 * @param dataType
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * 获取邮箱列表
	 * 
	 * @return Set<String>
	 */
	public Set<String> getMailArr() {
		return mailArr;
	}

	/**
	 * 设置邮箱列表
	 * 
	 * @param mailArr
	 */
	public void setMailArr(Set<String> mailArr) {
		this.mailArr = mailArr;
	}

}
