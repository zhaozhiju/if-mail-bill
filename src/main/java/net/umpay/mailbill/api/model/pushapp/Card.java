package net.umpay.mailbill.api.model.pushapp;

import java.util.List;

/**
 * 卡信息外层VO
 *  
 */
public class Card {

	private String dataType;			// 1卡列表 2账期列表 3月账单详情 4日账单详情 5账号绑定邮箱列表 6 解绑邮箱成功/失败
	private List<CardDetial> cardArr;	// 卡信息详情列表
	
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
	 * 获取卡信息详情列表
	 * 
	 * @return cardArr
	 */
	public List<CardDetial> getCardArr() {
		return cardArr;
	}
	
	/**
	 * 设置卡信息详情列表
	 * 
	 * @param cardArr
	 */
	public void setCardArr(List<CardDetial> cardArr) {
		this.cardArr = cardArr;
	}
	
}
