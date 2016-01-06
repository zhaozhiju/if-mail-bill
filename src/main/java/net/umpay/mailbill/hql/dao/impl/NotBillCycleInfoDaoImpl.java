package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.NotBillCycleInfoDao;
import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;

import org.springframework.stereotype.Repository;
/**
 * "邮件（非账单）周期表"(实现类)
 */
@Repository
public class NotBillCycleInfoDaoImpl extends HibernateDao<NotBillCycleInfoEntity, Long> implements
		NotBillCycleInfoDao {

	@Override
	public int existOfEmail(String oldhtml, String email) {
		String hql = "select senderUrl from NotBillCycleInfoEntity where oldHtmlUrl = ? and infoSource = ?";
		List<NotBillCycleInfoEntity> list = this.find(hql, oldhtml, email);
		return list.size();
	}
	
	@Override
	public NotBillCycleInfoEntity getDFSURlName(String newhtml, String card) {
		String hql = "from NotBillCycleInfoEntity where newHtmlUrl = ? and cardEndOfFour = ?";
		NotBillCycleInfoEntity findUnique = this.findUnique(hql, newhtml, card);
		return findUnique;
	}
	
	@Override
	public List<NotBillCycleInfoEntity> getDFSURlByNew(String newhtml) {
		List<NotBillCycleInfoEntity> findBy = this.findBy("newHtmlUrl", newhtml);
		return findBy;
	}

	@Override
	public List<NotBillCycleInfoEntity> getInfo(String emailUrl) {
		return this.findBy("infoSource", emailUrl);
	}
	
	@Override
	public List<NotBillCycleInfoEntity> findMonthExit(String userEmail, String month, int bankId, int billDate,
			int billType, String cardEndOfFour) {
		String hql = "from NotBillCycleInfoEntity where infoSource = ? and cardEndOfFour = ? and bankId = ?" +
				" and  billType = ? and accountOfDate = ? and billDate = ?";
		List<NotBillCycleInfoEntity> find = this.find(hql, userEmail, cardEndOfFour, bankId, billType, month, billDate);
		return find;
	}
	
	@Override
	public List<NotBillCycleInfoEntity> findDayExit(String userEmail, String day, int bankId,
			int billType, String cardEndOfFour) {
		String hql = "from NotBillCycleInfoEntity where infoSource = ? and cardEndOfFour = ? and bankId = ?" +
				" and  billType = ? and accountOfDate = ?";
		List<NotBillCycleInfoEntity> find = this.find(hql, userEmail, cardEndOfFour, bankId, billType, day);
		return find;
	}

	@Override
	public List<String> findExistEmailCount(String string) {
		String hql = "select distinct oldHtmlUrl from NotBillCycleInfoEntity where infoSource = ?";
		List<String> list = this.find(hql, string);
		return list;
	}

	@Override
	public List<NotBillCycleInfoEntity> findByEmailUrl(String emailUrl) {
		return this.findBy("infoSource", emailUrl);
	}

	@Override
	public NotBillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId) {
		return this.findUniqueBy("id", billCyclePkId);
	}

	@Override
	public void batchExecute(List<NotBillCycleInfoEntity> emailUrl) {
		String hql = "insert into NotBillCycleInfoEntity(senderUrl, userName, userGender, cardEndOfFour, bankId, cardType, billType, billCycleBegin, billCycleEnd, billDate,) value();";
		this.batchExecute(hql, emailUrl);
	}
}
