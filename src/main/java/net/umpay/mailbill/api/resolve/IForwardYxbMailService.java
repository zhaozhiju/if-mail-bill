package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.ForwardYxbMailEntity;

/**
 * 自建邮箱服务Service接口
 * @author admin
 *
 */
public interface IForwardYxbMailService {

	/**
	 * 保存实体
	 * @param entity
	 */
	public void saveForwardYxbMail(ForwardYxbMailEntity entity);
	
	/**
	 * 删除实体信息
	 * @param entity
	 */
	public void delectForwardYxbMail(ForwardYxbMailEntity entity);
	
	/**
	 * 更新实体信息
	 * @param entity
	 */
	public void updateForwardYxbMail(ForwardYxbMailEntity entity);
	
	/**
	 * 根据个人邮箱查询信息内容
	 * @param mailname	个人邮箱地址
	 * @return	 List<ForwardYxbMailEntity>
	 */
	public List<ForwardYxbMailEntity> findForwardYxbMailByMailname(String mailname);
	
	/**
	 * 根据自建邮箱查询信息内容
	 * @param yxbMailname	自建邮箱
	 * @return	List<ForwardYxbMailEntity>
	 */
	public List<ForwardYxbMailEntity> findForwardYxbMailByYxbMailname(String yxbMailname);
}
