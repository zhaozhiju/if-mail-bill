package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillLogDao;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

/**
 * 记录操作日志详细信息
 */
@Repository
public class BillLogDaoImpl extends HibernateDao<BillLogEntity, Long> implements BillLogDao {

	@Override
	public BillLogEntity findEntityById(long id) {
		return this.findUniqueBy("id", id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BillLogEntity> findEntityByEmail(String mail, boolean desc) {
		String hql = "from BillLogEntity where emailUrl = ? order by loginTime desc";
		Query createQuery = this.getSession().createQuery(hql);
		createQuery.setString(0, mail);
		List<BillLogEntity> list = createQuery.list();
		return list;
	}

}
