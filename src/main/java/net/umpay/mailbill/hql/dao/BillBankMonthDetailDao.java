package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;

/**
 * "邮件账单月账单明细"接口
 * @author admin
 *
 */
public interface BillBankMonthDetailDao extends BaseDao<BillBankMonthDetailEntity, Long>{

	/**
	 * 根据billCyclePkId获取相应的月账单详细内容
	 * @param billCyclePkId
	 * @return  List<BillBankMonthDetailEntity>
	 */
	public List<BillBankMonthDetailEntity> findByBillCyclePkId(Long billCyclePkId);

}
