package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IBillBankMonthDetailService;
import net.umpay.mailbill.hql.dao.BillBankMonthDetailDao;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 邮件账单月账单明细
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillBankMonthDetailServiceImpl implements
		IBillBankMonthDetailService {
	
	private static Logger log = LoggerFactory.getLogger(BillBankMonthDetailServiceImpl.class);
	@Autowired
	private BillBankMonthDetailDao billBankMonthDetailDao;
	
	@Override
	public void save(BillBankMonthDetailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billBankMonthDetailDao.save(entity);
	}

	@Transactional(readOnly = true)
	@Override
	public BillBankMonthDetailEntity getId(Long long1) {
		//log.info("method:{} \tservice:{}", new Object[]{"getId", this.getClass()});
		return billBankMonthDetailDao.get(long1);
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillBankMonthDetailEntity> findEntityByBillCyclePkId(Long billCyclePkId) {
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		return billBankMonthDetailDao.findByBillCyclePkId(billCyclePkId);
	}

	@Transactional(readOnly = true)
	@Override
	public Long[] getBillCycleId(boolean update,
			BillCycleInfoEntity billCycleInfoEntity) {
		//log.info("method:{} \tservice:{}", new Object[]{"getBillCycleId", this.getClass()});
		Long[] billId = null;
		if (update){
			List<BillBankMonthDetailEntity> findBy = this.findEntityByBillCyclePkId(billCycleInfoEntity.getId());
			billId = new Long[findBy.size()];
			for (int i = 0; i < findBy.size(); i++){
				billId[i] = findBy.get(i).getId();
			}
		}
		return billId;		
	}
	
	public BillBankMonthDetailDao getBillBankMonthDetailDao() {
		return billBankMonthDetailDao;
	}

	public void setBillBankMonthDetailDao(
			BillBankMonthDetailDao billBankMonthDetailDao) {
		this.billBankMonthDetailDao = billBankMonthDetailDao;
	}

}
