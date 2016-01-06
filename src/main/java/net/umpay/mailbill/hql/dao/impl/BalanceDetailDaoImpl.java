package net.umpay.mailbill.hql.dao.impl;

import net.umpay.mailbill.hql.dao.BalanceDetailDao;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * “本期应还总额信息详情”实现类
 */
@Repository
public class BalanceDetailDaoImpl extends HibernateDao<BalanceDetailEntity, Long> implements
		BalanceDetailDao {
}
