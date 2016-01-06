package net.umpay.mailbill.hql.dao.impl;

import net.umpay.mailbill.hql.dao.LeadInfoDao;
import net.umpay.mailbill.hql.model.LeadInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * 邮箱引导统计信息(pop3/imap4的开启提示)(接口)
 */

@Repository
public class LeadInfoDaoImpl extends HibernateDao<LeadInfoEntity, Long> implements LeadInfoDao {

}
