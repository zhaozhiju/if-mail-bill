package net.umpay.mailbill.api.resolve;

import net.umpay.mailbill.hql.model.SystemNotSpportInfoEntity;

/**
 * 系统未兼容的邮箱
 * @author admin
 */
public interface ISystemNotSpportInfoService {
	/**
	 * 保存实体类
	 * @param entity
	 */
	public void save(SystemNotSpportInfoEntity entity);
	/**
	 * 根据邮箱后缀查询是否存在该信息
	 * @param emailSuffix
	 * @return SystemNotSpportInfoEntity
	 */
	public SystemNotSpportInfoEntity findByEmailSuffix(String emailSuffix);
}
