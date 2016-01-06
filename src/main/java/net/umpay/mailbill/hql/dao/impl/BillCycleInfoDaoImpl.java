package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillCycleInfoDao;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * "邮件账单周期表"(实现类)
 */
@Repository
public class BillCycleInfoDaoImpl extends HibernateDao<BillCycleInfoEntity, Long> implements
		BillCycleInfoDao {

	@Override
	public int existOfEmail(String oldhtml, String email) {
		String hql = "select senderUrl from BillCycleInfoEntity where oldHtmlUrl = ? and infoSource = ?";
		List<BillCycleInfoEntity> list = this.find(hql, oldhtml, email);
		return list.size();
	}
	
	@Override
	public BillCycleInfoEntity getDFSURlName(String newhtml, String card) {
		String hql = "from BillCycleInfoEntity where newHtmlUrl = ? and cardEndOfFour = ?";
		BillCycleInfoEntity findUnique = this.findUnique(hql, newhtml, card);
		return findUnique;
	}
	
	@Override
	public List<BillCycleInfoEntity> getDFSURlByNew(String newhtml) {
		List<BillCycleInfoEntity> findBy = this.findBy("newHtmlUrl", newhtml);
		return findBy;
	}

	@Override
	public List<BillCycleInfoEntity> getInfo(String emailUrl) {
		return this.findBy("infoSource", emailUrl);
	}
	
	@Override
	public List<BillCycleInfoEntity> findMonthExit(String userEmail, String month, int bankId, int billDate,
			int billType, String cardEndOfFour) {
		String hql = "from BillCycleInfoEntity where infoSource = ? and cardEndOfFour = ? and bankId = ?" +
				" and  billType = ? and accountOfDate = ? and billDate = ?";
		List<BillCycleInfoEntity> find = this.find(hql, userEmail, cardEndOfFour, bankId, billType, month, billDate);
		return find;
	}
	
	@Override
	public List<BillCycleInfoEntity> findDayExit(String userEmail, String day, int bankId,
			int billType, String cardEndOfFour) {
		String hql = "from BillCycleInfoEntity where infoSource = ? and cardEndOfFour = ? and bankId = ?" +
				" and  billType = ? and accountOfDate = ?";
		List<BillCycleInfoEntity> find = this.find(hql, userEmail, cardEndOfFour, bankId, billType, day);
		return find;
	}

	@Override
	public List<String> findExistEmailCount(String string) {
		String hql = "select distinct oldHtmlUrl from BillCycleInfoEntity where infoSource = ?";
		List<String> list = this.find(hql, string);
		return list;
	}

	@Override
	public List<BillCycleInfoEntity> findMonthByEmailUrl(String emailUrl) {
		String hql = "from BillCycleInfoEntity where infoSource = ? and billType = ? order by accountOfDate desc";
		List<BillCycleInfoEntity> find = this.find(hql, emailUrl, MailBillTypeConstants.BILL_TYPE_MONTH);
		return find;
	}
	
	@Override
	public List<BillCycleInfoEntity> findDayByEmailUrl(String emailUrl) {
		String hql = "from BillCycleInfoEntity where infoSource = ? and billType = ? order by accountOfDate desc";
		List<BillCycleInfoEntity> find = this.find(hql, emailUrl, MailBillTypeConstants.BILL_TYPE_DAY);
		return find;
	}

	@Override
	public BillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId) {
		return this.findUniqueBy("id", billCyclePkId);
	}

	@Override
	public List<BillCycleInfoEntity> findInfoByInfoSource(int bankId, String infoSource,
			String cardEndOfFour) {
		String hql = "from BillCycleInfoEntity where bankId = ? and infoSource = ? and cardEndOfFour = ? order by accountOfDate desc";
		List<BillCycleInfoEntity> find = this.find(hql, bankId, infoSource, cardEndOfFour);
		return find;
	}

	@Override
	public List<BillCycleInfoEntity> findInfoByAccountId(int bankId, Long accountId,String cardEndOfFour) {
		String hql = "from BillCycleInfoEntity where bankId = ? and accountId = ? and cardEndOfFour = ? order by accountOfDate desc";
		List<BillCycleInfoEntity> find = this.find(hql, bankId, accountId, cardEndOfFour);
		return find;
	}

	@Override
	public Long findscVersionSEQ() {
		String sql = "select SEQ_CYCLE_INFO_SC_VERSION.NextVal from dual where 1=?";
		String find = this.findUniqueSql(sql, "1").toString();
		return Long.valueOf(find);
	}

	@Override
	public List<BillCycleInfoEntity> findCardsInfo(String mailUrl, Long accountId,
			String csVersion) {
		String hql = "from BillCycleInfoEntity where infoSource = ? and accountId = ? and scVersion > ? order by accountOfDate desc";
		List<BillCycleInfoEntity> find = this.find(hql, mailUrl, accountId, Long.valueOf(csVersion));
		return find;
	}

	@Override
	public List<BillCycleInfoEntity> findMonthtCylesInfo(String mailUrl,
			Long accountId, String csVersion, long bankId,
			String cardEndOfFour, Integer billType) {
		
		String hql = "from BillCycleInfoEntity where infoSource = ? and accountId = ? and scVersion > ? and bankId = ? and cardEndOfFour = ? and billType = ?";
		List<BillCycleInfoEntity> find = this.find(hql, mailUrl, accountId, Long.valueOf(csVersion), (int)bankId, cardEndOfFour, billType);
		return find;
	}

	@Override
	public List<BillCycleInfoEntity> findMonthtCylesInfoNoCardNum(
			String mailUrl, Long accountId, String csVersion, String userName,
			String paymentDate, long bankId, Integer billType) {
		// 只查询还款日 相同的, 如: 9号匹配 2014/11/09, 2014/10/09...
		if(StringUtils.isNotBlank(paymentDate)){
			String hql = "from BillCycleInfoEntity where infoSource = ? and accountId = ? and scVersion > ? and userName = ? and bankId = ? and To_Char(paymentDueDate,'dd') = ? and cardEndOfFour is null and billType = ?";
			List<BillCycleInfoEntity> find = this.find(hql, mailUrl, accountId, Long.valueOf(csVersion), userName, (int)bankId, paymentDate, billType);
			return find;
		} else {
			String hql = "from BillCycleInfoEntity where infoSource = ? and accountId = ? and scVersion > ? and userName = ? and bankId = ? and cardEndOfFour is null and billType = ?";
			List<BillCycleInfoEntity> find = this.find(hql, mailUrl, accountId, Long.valueOf(csVersion), userName, (int)bankId, billType);
			return find;
		}
	}
}
