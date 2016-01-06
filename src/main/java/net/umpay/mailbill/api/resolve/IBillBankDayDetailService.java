package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;

/**
 * 邮件账单日账单明细
 * @author admin
 *
 */
public interface IBillBankDayDetailService {
	
	/**
	 * 保存日账单明细实体
	 * @param entity
	 */
	public void save(BillBankDayDetailEntity entity);
	/**
	 * 根据cyclePkId来查询邮件账单明细
	 * 
	 * @param cyclePkId
	 * @return List<BillBankDayDetailEntity>
	 */
	public List<BillBankDayDetailEntity> findEntityByBillCyclePkId(Long cyclePkId);
}
