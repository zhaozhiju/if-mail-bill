package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillMainViceCardDao;
import net.umpay.mailbill.hql.model.BillMainViceCardEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * 主副卡关系设置
 */
@Repository
public class BillMainViceCardDaoImpl extends HibernateDao<BillMainViceCardEntity, Long> implements BillMainViceCardDao {

	@Override
	public List<BillMainViceCardEntity> findInfo(int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour) {
		String hql = "from BillMainViceCardEntity where bankId = ? and accountId = ? and " +
				"infoSourceEmail = ? and mainCardOfFour = ? ";
		List<BillMainViceCardEntity> find = this.find(hql, bankId, accountId, infoSourceEmail, mainCardOfFour);
		return find;
		
	}

}
