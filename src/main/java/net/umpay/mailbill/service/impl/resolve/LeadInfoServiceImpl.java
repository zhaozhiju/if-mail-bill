package net.umpay.mailbill.service.impl.resolve;

import net.umpay.mailbill.api.resolve.ILeadInfoService;
import net.umpay.mailbill.hql.dao.LeadInfoDao;
import net.umpay.mailbill.hql.model.LeadInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 邮箱引导统计信息
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class LeadInfoServiceImpl implements ILeadInfoService {
	
	private static Logger log = LoggerFactory.getLogger(LeadInfoServiceImpl.class);
	
	@Autowired
	private LeadInfoDao leadInfoDao;
	
	@Override
	public void save(LeadInfoEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		this.leadInfoDao.save(entity);
	}

	public LeadInfoDao getLeadInfoDao() {
		return leadInfoDao;
	}

	public void setLeadInfoDao(LeadInfoDao leadInfoDao) {
		this.leadInfoDao = leadInfoDao;
	}
}
