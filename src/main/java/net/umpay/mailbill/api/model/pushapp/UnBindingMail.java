package net.umpay.mailbill.api.model.pushapp;

/**
 * 用户解绑邮箱vo
 * @author zhaozj
 */
public class UnBindingMail {
	private String dataType; // 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	private String unbindingMailFlag; // 解绑邮箱 true成功 false失败

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
	 * 获取解绑邮箱 true成功 false失败
	 * 
	 * @return String
	 */
	public String getUnbindingMailFlag() {
		return unbindingMailFlag;
	}

	/**
	 * 设置解绑邮箱 true成功 false失败
	 * 
	 * @param unbindingMailFlag
	 */
	public void setUnbindingMailFlag(String unbindingMailFlag) {
		this.unbindingMailFlag = unbindingMailFlag;
	}
}
