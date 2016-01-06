package net.umpay.mailbill.hql.dao.impl;

import java.util.List;

import net.umpay.mailbill.hql.dao.BillUserInfoDao;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.hql.orm.hibernate.HibernateDao;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.security.DesUtil;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

/**
 * 邮件账单用户信息(dao的实现类)
 */
@Repository
public class BillUserInfoDaoImpl extends HibernateDao<BillUserInfoEntity, Long> implements
		BillUserInfoDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<BillUserInfoEntity> findMailByAccount(Long account) {
		String hql = "from BillUserInfoEntity where accountId=?";
		Query createQuery = this.createQuery(hql, account);
		return createQuery.list();
	}

	@Override
	public List<BillUserInfoEntity> findByMailUrl(String mailUrl) {
		String hql = "from BillUserInfoEntity where emailUrl=? and bindingStatus = 1";
		List<BillUserInfoEntity> find = this.find(hql, mailUrl);
		return find;
	}

	@Override
	public List<String> findMailUrlListByAccountId(Long accountId) {
		String hql = "select emailUrl from BillUserInfoEntity where accountId=? and bindingStatus = 1";
		List<String> find = this.find(hql, accountId);
		return find;
	}

	@Override
	public String findPasswordByMailUrl(String mailUrl) {
		String hql = "select password from BillUserInfoEntity where emailUrl=?";
		String find = this.findUnique(hql, mailUrl);
		return find;
	}

	@Override
	public int updateToUnbundling(Long accountId, String mailUrl) {
		String hql = "update BillUserInfoEntity set bindingStatus=0 where accountId = ? and emailUrl = ?";
		int countUpdate = this.batchExecute(hql, accountId, mailUrl);
		return countUpdate;
	}

	@Override
	public int updateToBinding(Long accountId, String mailUrl, String password) throws MailBillException {
		password = DesUtil.encrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
		String hql = "update BillUserInfoEntity set bindingStatus=1,password = ? where accountId = ? and emailUrl = ?";
		return this.batchExecute(hql, password, accountId, mailUrl);
	}
}
