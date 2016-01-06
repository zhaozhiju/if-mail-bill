package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
/**
 * 邮件账单月账单明细
 * @author admin
 *
 */
public interface IBillBankMonthDetailService {
	
	/**
	 * 保存实体
	 * @param entity
	 */
	public void save(BillBankMonthDetailEntity entity);

	/**
	 * 根据id来获取对象
	 * @param long1
	 * @return BillBankMonthDetailEntity
	 */
	public BillBankMonthDetailEntity getId(Long long1);
	
	/**
	 * 根据update的值，判断是否需要更新数据
	 * @param update	
	 * @param billCycleInfoEntity
	 * @return	Long[]
	 */
	public Long[] getBillCycleId(boolean update,BillCycleInfoEntity billCycleInfoEntity);
	
	/**
	 * 根据billCyclePkId获取相应的月账单详细内容
	 * @param billCyclePkId
	 * @return List<BillBankMonthDetailEntity>
	 */
	public List<BillBankMonthDetailEntity> findEntityByBillCyclePkId(Long billCyclePkId);

}
