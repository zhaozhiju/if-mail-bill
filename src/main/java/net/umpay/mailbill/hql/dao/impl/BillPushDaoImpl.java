package net.umpay.mailbill.hql.dao.impl;

import net.umpay.mailbill.hql.dao.BillPushDao;
import net.umpay.mailbill.hql.model.BillPushEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;


/**
 * 推送关系实现类
 */
@Repository
public class BillPushDaoImpl extends HibernateDao<BillPushEntity, Long> implements BillPushDao {

}
