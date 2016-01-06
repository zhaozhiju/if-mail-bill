package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IBalanceDetailService;
import net.umpay.mailbill.hql.dao.BalanceDetailDao;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 本期应还总额信息详情
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class) 
public class BalanceDetailServiceImpl implements IBalanceDetailService {
	
	private static Logger log = LoggerFactory.getLogger(BalanceDetailServiceImpl.class);
	
	@Autowired
	private BalanceDetailDao  balanceDetailDao;
	
	public void save(BalanceDetailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		balanceDetailDao.save(entity);
	}
	
	@Transactional(readOnly=true)
	public BalanceDetailEntity getId(Long long1) {
		//log.info("method:{} \tservice:{}", new Object[]{"getId", this.getClass()});
		return balanceDetailDao.get(long1);
	}
	
	@Transactional(readOnly=true)
	public List<BalanceDetailEntity> findEntityByBillCyclePkId(
			Long billCyclePkId) {
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		return balanceDetailDao.findBy("billCyclePkId", billCyclePkId);
	}

	@Transactional(readOnly=true)
	public Long[] getIdByBillCycle(boolean update,
			BillCycleInfoEntity billCycleInfoEntity, Long[] billId) {
		//log.info("method:{} \tservice:{}", new Object[]{"getIdByBillCycle", this.getClass()});
		if (update){
			List<BalanceDetailEntity> findBy = this.findEntityByBillCyclePkId(billCycleInfoEntity.getId());
			billId = new Long[findBy.size()];
			for (int i = 0; i < findBy.size(); i++){
				if (findBy.get(i).getCurrencyType().contains(MailBillTypeConstants.USD_CURRENCY_TYPE)){
					billId[1] = findBy.get(i).getId();
				}else{
					billId[0] = findBy.get(i).getId();
				}
			}
		}
		return billId;
	}

	public BalanceDetailDao getBalanceDetailDao() {
		return balanceDetailDao;
	}

	public void setBalanceDetailDao(BalanceDetailDao balanceDetailDao) {
		this.balanceDetailDao = balanceDetailDao;
	}
}
