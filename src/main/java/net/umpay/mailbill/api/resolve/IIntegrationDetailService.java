package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
/**
 * 本期积分汇总详情
 * @author admin
 */
public interface IIntegrationDetailService {
	/**
	 * 保存实体类
	 * @param entity
	 */
	public void save(IntegrationDetailEntity entity);
	
	/**
	 * 根据参数查询积分信息的id
	 * @param update
	 * @param billCycleInfoEntity
	 * @param billId
	 * @return	Long[]
	 */
	public Long[] getIdByBillCycleId(boolean update, BillCycleInfoEntity billCycleInfoEntity, Long[] billId) ;

	/**
	 * 根据账期表id查询积分详情
	 * @param billCyclePkId
	 * @return List<IntegrationDetailEntity>
	 */
	public List<IntegrationDetailEntity> findEntityByBillCyclePkId(Long billCyclePkId);
	
	/**
	 * 根据id获取实体内容
	 * @param id
	 * @return IntegrationDetailEntity
	 */
	public IntegrationDetailEntity getId(Long id);
	
}
