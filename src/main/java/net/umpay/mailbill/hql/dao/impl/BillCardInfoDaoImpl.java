package net.umpay.mailbill.hql.dao.impl;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.hql.dao.BillCardInfoDao;
import net.umpay.mailbill.hql.model.BillCardInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;

/**
 * "卡信息表"(实现类)
 */
@Repository
public class BillCardInfoDaoImpl extends HibernateDao<BillCardInfoEntity, Long> implements BillCardInfoDao{

	@Override
	public List<BillCardInfoEntity> findInfo(int bankId, long accountId, String mainCardOfFour) {
		String hql = "from BillCardInfoEntity where bankId = ? and accountId = ? and mainCardOfFour = ?";
		List<BillCardInfoEntity> find = this.find(hql, bankId, accountId, mainCardOfFour);
		return find;
	}

	@Override
	public int updateBillDate(Integer billDate, int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour) {
		String hql = "update BillCardInfoEntity set billDate=? where bankId = ? and accountId = ? and " +
				"infoSourceEmail = ? and mainCardOfFour = ? ";
		int countUpdate = this.batchExecute(hql, billDate, bankId, accountId, infoSourceEmail, mainCardOfFour);
		return countUpdate;
	}

	@Override
	public int updatePaymentDueDate(Date PaymentDueDate, int bankId, long accountId,
			String infoSourceEmail, String mainCardOfFour) {
		String hql = "update BillCardInfoEntity set paymentDueDate=? where bankId = ? and accountId = ? and " +
				"infoSourceEmail = ? and mainCardOfFour = ? ";
		int countUpdate = this.batchExecute(hql, PaymentDueDate, bankId, accountId, infoSourceEmail, mainCardOfFour);
		return countUpdate;
	}

	@Override
	public int updateCardType(String cardType, int bankId, long accountId,
			String infoSourceEmail, String mainCardOfFour) {
		String hql = "update BillCardInfoEntity set cardType=? where bankId = ? and accountId = ? and " +
				"infoSourceEmail = ? and mainCardOfFour = ? ";
		int countUpdate = this.batchExecute(hql, cardType, bankId, accountId, infoSourceEmail, mainCardOfFour);
		return countUpdate;
	}
}
