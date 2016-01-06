package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillMainViceCardEntity;
/**
 *  主副卡关系设置接口
 */
public interface BillMainViceCardDao extends BaseDao<BillMainViceCardEntity, Long> {

	/**
	 * 在主副卡关系内查询信息
	 * 
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @return List<BillMainViceCardEntity>
	 */
	public List<BillMainViceCardEntity> findInfo(int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour);

	
	
}
