package net.umpay.mailbill.api.resolve;

import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
/**
 * 本期应还总额信息详情
 * @author admin
 *
 */
public interface IBalanceDetailService {
	/**
	 * 保存实体
	 * @param entity 实体
	 */
	public void save(BalanceDetailEntity entity);
	/**
	 * 根据update的值来判断是否需要更新，用billId的值来存取需要更新的数据的Id
	 * @param update
	 * @param billCycleInfoEntity	
	 * @param billId
	 * @return Long[]
	 */
	public Long[] getIdByBillCycle(boolean update,
			BillCycleInfoEntity billCycleInfoEntity, Long[] billId);
}
