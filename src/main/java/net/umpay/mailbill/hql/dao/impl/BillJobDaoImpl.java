package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillJobDao;
import net.umpay.mailbill.hql.model.BillJobEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.springframework.stereotype.Repository;

/**
 * 邮件账单定时任务实现类
 */
@Repository
public class BillJobDaoImpl extends HibernateDao<BillJobEntity, Long> implements
		BillJobDao {

	@Override
	public List<BillJobEntity> findBillDateAndDay(int day) {
		String hql = "from BillJobEntity where billDate = ? and billType = ?";
		return this.find(hql, day, MailBillTypeConstants.BILL_TYPE_MONTH);
	}
	
	@Override
	public List<BillJobEntity> findBillTypeAndDay() {
		return this.findBy("billType", MailBillTypeConstants.BILL_TYPE_DAY);
	}

	@Override
	public boolean findJob(int bankId, int billDate, int billType,
			String cardEndOfFour, String userEmail) {
		String hql = "select cardEndOfFour, userEmail, billDate, billType, bankId from BillJobEntity where bankId = ? and  billDate = ? and " +
				"billType = ? and cardEndOfFour = ? and userEmail = ?";
		List<Object> list = this.find(hql, bankId, billDate, billType, cardEndOfFour, userEmail);
		return list.size() == 0 ? false : true;
	}
	
	@Override
	public boolean findDayJob(int bankId, int billType,
			String cardEndOfFour, String userEmail) {
		String hql = "select cardEndOfFour, userEmail, billDate, billType, bankId from BillJobEntity where bankId = ? and " +
				"billType = ? and cardEndOfFour = ? and userEmail = ?";
		List<Object> list = this.find(hql, bankId, MailBillTypeConstants.BILL_TYPE_DAY, cardEndOfFour, userEmail);
		return list.size() == 0 ? false : true;
	}

}
