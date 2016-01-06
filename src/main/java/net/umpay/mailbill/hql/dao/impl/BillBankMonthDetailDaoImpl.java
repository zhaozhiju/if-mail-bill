package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillBankMonthDetailDao;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;
/**
 * "邮件账单月账单明细"
 */
@Repository
public class BillBankMonthDetailDaoImpl extends HibernateDao<BillBankMonthDetailEntity, Long> implements
		BillBankMonthDetailDao {

	@Override
	public List<BillBankMonthDetailEntity> findByBillCyclePkId(
			Long billCyclePkId) {
		return this.findBy("billCyclePkId", billCyclePkId);
	}

}
