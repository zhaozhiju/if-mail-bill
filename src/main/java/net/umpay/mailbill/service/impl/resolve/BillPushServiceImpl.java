package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IBillPushService;
import net.umpay.mailbill.hql.dao.BillPushDao;
import net.umpay.mailbill.hql.model.BillPushEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 推送消息明细
 * @author admin
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillPushServiceImpl implements IBillPushService {

	private static Logger log = LoggerFactory.getLogger(BillPushServiceImpl.class);
	
	@Autowired
	private BillPushDao pushDao;

	@Override
	public void save(BillPushEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		pushDao.save(entity);
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillPushEntity> findByAccountId(Long accountId) {
		//log.info("method:{} \tservice:{}", new Object[]{"findByAccountId", this.getClass()});
		List<BillPushEntity> findBy = pushDao.findBy("accountId", accountId);
		return findBy;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillPushEntity> findAll() {
		return pushDao.getAll();
	}

	public BillPushDao getPushDao() {
		return pushDao;
	}

	public void setPushDao(BillPushDao pushDao) {
		this.pushDao = pushDao;
	}

}
