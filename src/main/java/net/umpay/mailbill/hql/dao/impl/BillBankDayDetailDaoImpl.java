package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillBankDayDetailDao;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;
/**
 * "邮件账单日账单明细"
 */
@Repository
public class BillBankDayDetailDaoImpl extends HibernateDao<BillBankDayDetailEntity, Long> implements
		BillBankDayDetailDao {

	@Override
	public List<BillBankDayDetailEntity> findEntityByBillCyclePkId(
			Long cyclePkId) {
		return this.findBy("billCyclePkId", cyclePkId);
	}

}
