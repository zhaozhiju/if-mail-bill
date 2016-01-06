package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillPushEntity;

/**
 * 推送关系信息接口
 * @author admin
 */
public interface IBillPushService {
	
	/**
	 * 保存
	 */
	public void save(BillPushEntity entity);
	
	/**
	 * 根据accountId查询推送表里是否有此条数据
	 * @param accountId		用户唯一标示
	 * @return List<BillPushEntity>	
	 */
	public List<BillPushEntity> findByAccountId(Long accountId);
	/**
	 * 查询所有的
	 * @return List<BillPushEntity>	
	 */
	public List<BillPushEntity> findAll();
}
