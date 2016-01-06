package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IIntegrationDetailService;
import net.umpay.mailbill.hql.dao.IntegrationDetailDao;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 本期积分汇总详情
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class IntegrationDetailServiceImpl implements IIntegrationDetailService {

	private static Logger log = LoggerFactory.getLogger(IntegrationDetailServiceImpl.class);
	
	@Autowired
	private IntegrationDetailDao integrationDetailDao;
	
	@Override
	public void save(IntegrationDetailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		integrationDetailDao.save(entity);
	}
	
	@Transactional(readOnly = false)
	@Override
	public List<IntegrationDetailEntity> findEntityByBillCyclePkId(
			Long billCyclePkId) {
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		return integrationDetailDao.findBy("billCyclePkId", billCyclePkId);
	}
	
	@Transactional(readOnly = true)
	@Override
	public IntegrationDetailEntity getId(Long id) {
		//log.info("method:{} \tservice:{}", new Object[]{"getId", this.getClass()});
		return integrationDetailDao.get(id);
	}

	@Transactional(readOnly = true)
	@Override
	public Long[] getIdByBillCycleId(boolean update,
			BillCycleInfoEntity billCycleInfoEntity, Long[] billId) {
		//log.info("method:{} \tservice:{}", new Object[]{"getIdByBillCycleId", this.getClass()});
		if (update){
			List<IntegrationDetailEntity> findBy = this.findEntityByBillCyclePkId(billCycleInfoEntity.getId());
			billId = new Long[findBy.size()];
			for (int i = 0; i < findBy.size(); i++){
				if (findBy.get(i).getCurrencyType().contains(MailBillTypeConstants.USD_CURRENCY_TYPE) || findBy.get(i).getCurrencyType().contains("美元")){
					billId[1] = findBy.get(i).getId();
				}else{
					billId[0] = findBy.get(i).getId();
				}
			}
		}
		return billId;
	}

	public IntegrationDetailDao getIntegrationDetailDao() {
		return integrationDetailDao;
	}

	public void setIntegrationDetailDao(IntegrationDetailDao integrationDetailDao) {
		this.integrationDetailDao = integrationDetailDao;
	}

}