package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillLogEntity;


/**
 * 记录操作日志详细信息
 */
public interface BillLogDao extends BaseDao<BillLogEntity, Long>{

	public BillLogEntity findEntityById(long id);

	/**
	 * 根据邮箱地址取出时间最近的
	 * @param mail		邮箱地址
	 * @param desc		是否排序（默认true）
	 * @return			List<BillLogEntity>
	 */
	public List<BillLogEntity> findEntityByEmail(String mail, boolean desc);
	
}
