package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.resolve.IBillUserInfoService;
import net.umpay.mailbill.hql.dao.BillUserInfoDao;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.security.DesUtil;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮件账单用户信息
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillUserInfoServiceImpl implements IBillUserInfoService {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(BillUserInfoServiceImpl.class);
	@Autowired
	private BillUserInfoDao billUserInfoDao;
	
	
	@Override
	public void save(String emailUrl, String password, Long accountId) throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		BillUserInfoEntity entity = new BillUserInfoEntity();
		entity.setEmailUrl(emailUrl);
		entity.setPassword(DesUtil.encrypt3DES(Constants.PASSWORDKEY.getBytes(), password));
		entity.setAccountId(accountId);
		entity.setBindingDate(new Date());
		// 获取该用户绑定邮箱的数量
		List<BillUserInfoEntity> part = billUserInfoDao.findMailByAccount(accountId);
		if(CollectionUtils.isNotEmpty(part)){
			int size = part.size();
			if(size < Integer.parseInt(Constants.ACCOUNT_BINDING_MAIL_LIMIT)){
				billUserInfoDao.save(entity);
			} else {
				// 超出的用户绑定邮箱的数量的上限阀值
				throw MailBillExceptionUtil.getWithLog(
						ErrorCodeContants.ACCOUNT_BINDING_MAIL_OVERFLOE_LIMIT_CODE,
						ErrorCodeContants.ACCOUNT_BINDING_MAIL_OVERFLOE_LIMIT.getMsg(), log);
			}
		}else{
			billUserInfoDao.save(entity);
		}
	}
	
	@Transactional(readOnly = true)  
	@Override
	public BillUserInfoEntity findUser(String emailUrl) {
		//log.info("method:{} \tservice:{}", new Object[]{"findUser", this.getClass()});
		List<BillUserInfoEntity> findBy = null;
		findBy = billUserInfoDao.findBy("emailUrl", emailUrl);
		if (findBy.size() != 0){
			BillUserInfoEntity billUserInfoEntity = findBy.get(0);
			return billUserInfoEntity;
		}else{
			return null;
		}
	}
	
	/**
	 *  查询邮箱用户是否已绑定
	 *  
	 * @param emailUrl	邮箱地址
	 * @param accountId 用户账号
	 * @return  boolean	true 已绑定; false 未绑定;
	 * add by zhaozj on 2014/10/20
	 */
	@Transactional(readOnly = true)
	@Override
	public boolean userIfBindingMail(String emailUrl, Long accountId) throws MailBillException {
		boolean flag = false;
		String hql = "from BillUserInfoEntity where emailUrl = ? and  accountId = ?";
		List<BillUserInfoEntity> billUserInfoEntityList =  billUserInfoDao.find(hql, emailUrl, accountId);
		if(CollectionUtils.isNotEmpty(billUserInfoEntityList)){
			flag = true;
		}
		return flag;
	}
	
	@Transactional(readOnly = true)  
	@Override
	public List<BillUserInfoEntity> findAll() throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[]{"findAll", this.getClass()});
		String decrypt3des = null;
		String password = null;
		List<BillUserInfoEntity> all = billUserInfoDao.getAll();
		for (BillUserInfoEntity billUserInfoEntity: all){
			password = billUserInfoEntity.getPassword();
			decrypt3des = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
			billUserInfoEntity.setPassword(decrypt3des);
		}
		return all;
	}

	@Transactional(readOnly = true)  
	@Override
	public List<BillUserInfoEntity> findMailByAccount(Long account) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findMailByAccount", this.getClass()});
		String decrypt3des = null;
		String password = null;
		List<BillUserInfoEntity> part = billUserInfoDao.findMailByAccount(account);
		for (BillUserInfoEntity billUserInfoEntity: part){
			password = billUserInfoEntity.getPassword();
			decrypt3des = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
			billUserInfoEntity.setPassword(decrypt3des);
		}
		return part;
	}
	
	@Override
	public int updateToBinding(Long accountId, String mailUrl, String password) throws MailBillException {
		return billUserInfoDao.updateToBinding(accountId, mailUrl, password);
	}

	@Override
	public List<BillUserInfoEntity> findByMailUrl(String mailUrl) {
		return billUserInfoDao.findByMailUrl(mailUrl);
	}

	@Override
	public List<String> findMailUrlList(Long accountId) {
		List<String> mailList = billUserInfoDao.findMailUrlListByAccountId(accountId);
		return mailList;
	}

	@Override
	public String findPasswordByMailUrl(String mailUrl) {
		return billUserInfoDao.findPasswordByMailUrl(mailUrl);
	}

	@Override
	public int updateToUnbundling(Long accountId, String mailUrl) {
		return billUserInfoDao.updateToUnbundling(accountId, mailUrl);
	}
}
