package net.umpay.mailbill.api.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.hql.model.BillCardInfoEntity;

/**
 * 用户自定义卡信息接口 
 */
public interface IBillCardInfoService {
	
	/**
	 * 保存数据
	 * 
	 * @param billCardInfoEntity
	 */
	public void save(BillCardInfoEntity billCardInfoEntity);

	/**
	 * 在卡信息表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param mainCardOfFour		主卡卡号
	 * @return List<BillCardInfoEntity>
	 */
	public List<BillCardInfoEntity> find(int bankId, long accountId, String mainCardOfFour);
	
	/**
	 * 设置账单日
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @param billDate				账单日
	 */
	public int setBillDate(Integer bankId, Long accountId, String infoSourceEmail, String mainCardOfFour, Integer billDate);
	
	/**
	 * 设置还款日
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @param paymentDueDate		还款日
	 */
	public int setPaymentDueDate(Integer bankId, Long accountId, String infoSourceEmail, String mainCardOfFour, Date paymentDueDate);
	
	/**
	 * 设置卡品牌
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @param cardType				卡品牌
	 * @return int
	 */
	public int setCardType(Integer bankId, Long accountId, String infoSourceEmail, String mainCardOfFour, String cardType);
}
