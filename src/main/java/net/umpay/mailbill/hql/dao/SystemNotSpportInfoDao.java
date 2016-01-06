package net.umpay.mailbill.hql.dao;

import net.umpay.mailbill.hql.model.SystemNotSpportInfoEntity;
/**
 * 系统未兼容的邮箱(接口)
 */
public interface SystemNotSpportInfoDao extends BaseDao<SystemNotSpportInfoEntity, Long> {

	public SystemNotSpportInfoEntity findByEmailSuffix(String emailSuffix);

}
