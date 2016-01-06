package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IBillBankDayDetailService;
import net.umpay.mailbill.hql.dao.BillBankDayDetailDao;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 邮件账单日账单明细
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillBankDayDetailServiceImpl implements IBillBankDayDetailService {

	private static Logger log = LoggerFactory.getLogger(BillBankDayDetailServiceImpl.class);
	
	@Autowired
	private BillBankDayDetailDao billBankDayDetailDao;
	@Override
	public void save(BillBankDayDetailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billBankDayDetailDao.save(entity);
		
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillBankDayDetailEntity> findEntityByBillCyclePkId(Long cyclePkId) {
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		return billBankDayDetailDao.findEntityByBillCyclePkId(cyclePkId);
	}
	
	public BillBankDayDetailDao getBillBankDayDetailDao() {
		return billBankDayDetailDao;
	}
	public void setBillBankDayDetailDao(BillBankDayDetailDao billBankDayDetailDao) {
		this.billBankDayDetailDao = billBankDayDetailDao;
	}

}
