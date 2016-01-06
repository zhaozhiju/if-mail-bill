package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillJobTempEntity;

/**
 * 临时任务表Dao层
 * @author admin
 *
 */
public interface BillJobTempDao extends BaseDao<BillJobTempEntity, Long> {

	/**
	 * 查询出月账单所有任务
	 * 
	 * @return List<BillJobTempEntity>
	 */
	public List<BillJobTempEntity> findMonthAll();
	/**
	 * 查询出日账单所有任务
	 * 
	 * @return List<BillJobTempEntity>
	 */
	public List<BillJobTempEntity> findDayAll();
	/**
	 * 查询出指定任务
	 * 
	 * @return List<BillJobTempEntity>
	 */
	public List<BillJobTempEntity> find(String card, String eamil, int billType);

}
