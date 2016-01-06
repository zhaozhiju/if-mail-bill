package net.umpay.mailbill.service.impl.resolve;

import net.umpay.mailbill.api.resolve.ISystemNotSpportInfoService;
import net.umpay.mailbill.hql.dao.SystemNotSpportInfoDao;
import net.umpay.mailbill.hql.model.SystemNotSpportInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统未兼容的邮箱
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class SystemNotSpportInfoServiceImpl implements
		ISystemNotSpportInfoService {
	private static Logger log = LoggerFactory.getLogger(SystemNotSpportInfoServiceImpl.class);
	@Autowired
	private SystemNotSpportInfoDao systemNotSpportInfoDao;
	
	@Override
	public void save(SystemNotSpportInfoEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		systemNotSpportInfoDao.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public SystemNotSpportInfoEntity findByEmailSuffix(String emailSuffix) {
		return systemNotSpportInfoDao.findByEmailSuffix(emailSuffix);
	}
	
	public SystemNotSpportInfoDao getSystemNotSpportInfoDao() {
		return systemNotSpportInfoDao;
	}

	public void setSystemNotSpportInfoDao(
			SystemNotSpportInfoDao systemNotSpportInfoDao) {
		this.systemNotSpportInfoDao = systemNotSpportInfoDao;
	}


}
