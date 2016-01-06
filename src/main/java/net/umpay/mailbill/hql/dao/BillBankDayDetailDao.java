package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;


/**
 * “邮件账单日账单明细”（接口）
 * @author admin
 *
 */
public interface BillBankDayDetailDao extends BaseDao<BillBankDayDetailEntity, Long>{

	/**
	 * 根据cyclePkId来查询日账单的详细信息
	 * @param cyclePkId
	 * @return List<BillBankDayDetailEntity>
	 */
	public List<BillBankDayDetailEntity> findEntityByBillCyclePkId(Long cyclePkId);
}
