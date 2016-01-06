package net.umpay.mailbill.api.model.pushapp;

/**
 * 账号绑定邮箱列表VO
 *  
 */
public class BindingMail {
	
	private String dataType;	//  1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	private String[] mailArr;	// 账号绑定的有效邮箱列表
	
	/**
	 * 获取dataType
	 * 
	 * @return dataType 
	 */
	public String getDataType() {
		return dataType;
	}
	
	/**
	 * 设置dataType
	 * 
	 * @param dataType
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	/**
	 * 获取账号绑定的有效邮箱列表
	 * 
	 * @return mailArr
	 */
	public String[] getMailArr() {
		return mailArr;
	}
	
	/**
	 * 设置账号绑定的有效邮箱列表
	 * 
	 * @param mailArr
	 */
	public void setMailArr(String[] mailArr) {
		this.mailArr = mailArr;
	}
}
