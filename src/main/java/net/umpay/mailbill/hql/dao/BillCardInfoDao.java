package net.umpay.mailbill.hql.dao;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.hql.model.BillCardInfoEntity;

/**
 * "卡信息表dao"
 *
 *
 */
public interface BillCardInfoDao extends BaseDao<BillCardInfoEntity,Long>{
	
	/**
	 * 在卡信息表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param mainCardOfFour		主卡卡号
	 * @return List<BillCardInfoEntity>
	 */
	public List<BillCardInfoEntity> findInfo(int bankId, long accountId, String mainCardOfFour);
	
	/**
	 * 在卡信息表中按条件更新账单日
	 * 
	 * @param billDate				账单日
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 */
	public int updateBillDate(Integer billDate, int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour);

	/**
	 * 在卡信息表中按条件更新还款日
	 * 
	 * @param paymentDate			还款日
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 */
	public int updatePaymentDueDate(Date paymentDate, int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour);
	/**
	 * 在卡信息表中按条件更新卡种
	 * 
	 * @param cardType				卡种
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 */
	public int updateCardType(String cardType, int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour);
}
