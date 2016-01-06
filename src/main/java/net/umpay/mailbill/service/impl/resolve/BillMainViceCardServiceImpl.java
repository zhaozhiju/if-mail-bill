package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.resolve.IBillMainViceCardService;
import net.umpay.mailbill.hql.dao.BillMainViceCardDao;
import net.umpay.mailbill.hql.model.BillMainViceCardEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 主副卡关系设置
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillMainViceCardServiceImpl implements IBillMainViceCardService {

	private static Logger log = LoggerFactory.getLogger(BillMainViceCardServiceImpl.class);
	@Autowired
	private BillMainViceCardDao billMainViceCardDao;

	@Override
	public void save(int bankId, Long accountId, String infoSourceEmail,
			String mainCardOfFour, String viceCardOfFour) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		BillMainViceCardEntity entity = new BillMainViceCardEntity();
		entity.setAccountId(accountId);
		entity.setBankId(bankId);
		entity.setCreateTime(new Date());
		entity.setInfoSourceEmail(infoSourceEmail);
		entity.setMainCardOfFour(mainCardOfFour);
		entity.setViceCardOfFour(viceCardOfFour);
		billMainViceCardDao.save(entity);
	}
	
	@Override
	public void update(BillMainViceCardEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"update", this.getClass()});
		entity.setUpdateTime(new Date());
		billMainViceCardDao.save(entity);
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillMainViceCardEntity> find(int bankId, long accountId, String infoSourceEmail,
			String mainCardOfFour) {
		//log.info("method:{} \tservice:{}", new Object[]{"find", this.getClass()});
		List<BillMainViceCardEntity> findInfo = billMainViceCardDao.findInfo(bankId, accountId, infoSourceEmail, mainCardOfFour);
		return findInfo;
	}

	public BillMainViceCardDao getBillMainViceCardDao() {
		return billMainViceCardDao;
	}
	
	public void setBillMainViceCardDao(BillMainViceCardDao billMainViceCardDao) {
		this.billMainViceCardDao = billMainViceCardDao;
	}



}
