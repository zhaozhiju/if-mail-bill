package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillJobTempDao;
import net.umpay.mailbill.hql.model.BillJobTempEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.springframework.stereotype.Repository;
/**
 * 邮件账单定时任务临时表dao实现类
 */
@Repository
public class BillJobDaoTempImpl extends HibernateDao<BillJobTempEntity, Long> implements
		BillJobTempDao {

	@Override
	public List<BillJobTempEntity> findMonthAll() {
		String hql = "from BillJobTempEntity where isVaild = ? and billType = ?";
		List<BillJobTempEntity> findBy = this.find(hql, 1, MailBillTypeConstants.BILL_TYPE_MONTH);
		return findBy;
	}
	
	@Override
	public List<BillJobTempEntity> findDayAll() {
		String hql = "from BillJobTempEntity where isVaild = ? and billType = ?";
		List<BillJobTempEntity> findBy = this.find(hql, 1, MailBillTypeConstants.BILL_TYPE_DAY);
		return findBy;
	}

	@Override
	public List<BillJobTempEntity> find(String card, String email, int billType) {
		String hql = "from BillJobTempEntity where cardEndOfFour = ? and userEmail = ? and billType = ?";
		return this.find(hql, card, email, billType);
	}

}
