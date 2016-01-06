package net.umpay.mailbill.hql.dao.impl;

import net.umpay.mailbill.hql.dao.SystemNotSpportInfoDao;
import net.umpay.mailbill.hql.model.SystemNotSpportInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * 系统未兼容的邮箱(实现类)
 */
@Repository
public class SystemNotSpportInfoDaoImpl extends HibernateDao<SystemNotSpportInfoEntity, Long> implements
		SystemNotSpportInfoDao {

	@Override
	public SystemNotSpportInfoEntity findByEmailSuffix(String emailSuffix) {
		return this.findUniqueBy("emailSuffix", emailSuffix);
	}

}
