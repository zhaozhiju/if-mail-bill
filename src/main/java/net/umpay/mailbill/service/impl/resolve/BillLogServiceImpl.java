package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.resolve.IBillLogService;
import net.umpay.mailbill.hql.dao.BillLogDao;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.util.security.RandomUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 记录操作日志详细信息
 * @author admin
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillLogServiceImpl implements IBillLogService {

	private static Logger log = LoggerFactory.getLogger(BillLogServiceImpl.class);
	
	@Autowired
	private BillLogDao billLogDao;
	
	@Override
	public void save(BillLogEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billLogDao.save(entity);
	}

	@Transactional(readOnly = true)
	@Override
	public BillLogEntity findEntityById(long id) {
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		if (id == 0){
			return null;
		}else{
			return billLogDao.findEntityById(id);
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public BillLogEntity getBillLog(String phoneId, Long accountId, String emailUrl){
		//日志操作
		BillLogEntity billLogEntity = new BillLogEntity();
		//TODO 暂时在此赋值
//		accountId = RandomUtil.getUniqueBigDecimal(10).longValue();
		phoneId = RandomUtil.getUniqueBigDecimal(10).toString();
		billLogEntity.setEmailUrl(emailUrl);
		billLogEntity.setLoginTime(new Date());
		billLogEntity.setAccountId(accountId);
		billLogEntity.setPhoneId(phoneId);
		return billLogEntity;
	}
	
	@Override
	public BillLogEntity findEntityByEmail(String mail) {
		boolean  desc = true;
		List<BillLogEntity> findBy = billLogDao.findEntityByEmail(mail, desc);
		return findBy.get(0);
	}	

	public BillLogDao getBillLogDao() {
		return billLogDao;
	}

	public void setBillLogDao(BillLogDao billLogDao) {
		this.billLogDao = billLogDao;
	}
}
