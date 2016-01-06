package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.ForwardYxbMailEntity;

/**
 * 自建邮箱服务dao层
 */
public interface ForwardYxbMailDao  extends BaseDao<ForwardYxbMailEntity, Long>{

	/**
	 * 根据自建邮箱的地址查询信息
	 * @param yxbMailname	自建邮箱地址
	 * @return	List<ForwardYxbMailEntity>
	 */
	public List<ForwardYxbMailEntity> findForwardYxbMailByYxbMailname(String yxbMailname);

	/**
	 * 根据个人邮箱的地址查询信息
	 * @param mailname	个人邮箱地址
	 * @return	List<ForwardYxbMailEntity>
	 */
	public List<ForwardYxbMailEntity> findForwardYxbMailByMailname(String mailname);

	
}
