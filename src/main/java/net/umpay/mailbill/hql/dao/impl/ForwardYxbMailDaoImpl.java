package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.ForwardYxbMailDao;
import net.umpay.mailbill.hql.model.ForwardYxbMailEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * 自建邮箱服务实现类
 */
@Repository
public class ForwardYxbMailDaoImpl extends HibernateDao<ForwardYxbMailEntity, Long> implements ForwardYxbMailDao {

	@Override
	public List<ForwardYxbMailEntity> findForwardYxbMailByYxbMailname(String yxbMailname) {
		return this.findBy("yxbMailName", yxbMailname);
	}

	@Override
	public List<ForwardYxbMailEntity> findForwardYxbMailByMailname(String mailname) {
		return this.findBy("mailName", mailname);
	}
	
}
