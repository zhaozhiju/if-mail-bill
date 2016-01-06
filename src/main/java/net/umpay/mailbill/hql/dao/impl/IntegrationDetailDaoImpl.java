package net.umpay.mailbill.hql.dao.impl;

import net.umpay.mailbill.hql.dao.IntegrationDetailDao;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * 本期积分汇总详情(实现类)
 */
@Repository
public class IntegrationDetailDaoImpl extends HibernateDao<IntegrationDetailEntity, Long>
		implements IntegrationDetailDao {

}
