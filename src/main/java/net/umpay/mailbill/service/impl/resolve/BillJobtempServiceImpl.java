package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.job.BillJobTempView;
import net.umpay.mailbill.api.resolve.IBillJobTempService;
import net.umpay.mailbill.hql.dao.BillJobTempDao;
import net.umpay.mailbill.hql.model.BillJobTempEntity;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 邮件临时表Service
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillJobtempServiceImpl implements IBillJobTempService {
	
	private static Logger log = LoggerFactory.getLogger(BillJobtempServiceImpl.class);
	
	@Autowired
	private BillJobTempDao billJobTempDao;
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	
	@Transactional(readOnly = true)
	@Override
	public List<BillJobTempView> findMonthAll() {
		//log.info("method:{} \tservice:{}", new Object[]{"findMonthAll", this.getClass()});
		List<BillJobTempEntity> findAll = billJobTempDao.findMonthAll();
		List<BillJobTempView> billJobTempViews = new ArrayList<BillJobTempView>();
		BillUserInfoEntity findUser = null;
		for (BillJobTempEntity billJobTempEntity : findAll){
			findUser = null;
			BillJobTempView tempView = new BillJobTempView();
			tempView.setId(billJobTempEntity.getId());
			tempView.setAcountId(billJobTempEntity.getAccountId());
			tempView.setBankId(billJobTempEntity.getBankId());
			tempView.setBillDate(billJobTempEntity.getBillDate());
			tempView.setBillEndDate(billJobTempEntity.getBillEndDate());
			tempView.setBillStartDate(billJobTempEntity.getBillStartDate());
			tempView.setBillType(billJobTempEntity.getBillType());
			tempView.setCardEndOfFour(billJobTempEntity.getCardEndOfFour());
			tempView.setIsVaild(billJobTempEntity.getIsVaild());
			String userEmail = billJobTempEntity.getUserEmail();
			tempView.setUserEmail(userEmail);
			if (!StringUtils.isBlank(userEmail)){
				findUser = billUserInfoServiceImpl.findUser(userEmail);
				tempView.setPassword(findUser.getPassword());
			}
			billJobTempViews.add(tempView);
		}
		return billJobTempViews;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillJobTempView> findDayAll() {
		//log.info("method:{} \tservice:{}", new Object[]{"findDayAll", this.getClass()});
		List<BillJobTempEntity> findAll = billJobTempDao.findDayAll();
		List<BillJobTempView> billJobTempViews = new ArrayList<BillJobTempView>();
		BillUserInfoEntity findUser = null;
		for (BillJobTempEntity billJobTempEntity : findAll){
			findUser = null;
			BillJobTempView tempView = new BillJobTempView();
			tempView.setId(billJobTempEntity.getId());
			tempView.setAcountId(billJobTempEntity.getAccountId());
			tempView.setBankId(billJobTempEntity.getBankId());
			tempView.setBillEndDate(billJobTempEntity.getBillEndDate());
			tempView.setBillStartDate(billJobTempEntity.getBillStartDate());
			tempView.setBillType(billJobTempEntity.getBillType());
			tempView.setCardEndOfFour(billJobTempEntity.getCardEndOfFour());
			tempView.setIsVaild(billJobTempEntity.getIsVaild());
			String userEmail = billJobTempEntity.getUserEmail();
			tempView.setUserEmail(userEmail);
			if (!StringUtils.isBlank(userEmail)){
				findUser = billUserInfoServiceImpl.findUser(userEmail);
				tempView.setPassword(findUser.getPassword());
			}
			billJobTempViews.add(tempView);
		}
		return billJobTempViews;
	}

	@Override
	public void save(BillJobTempEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billJobTempDao.save(entity);
	}
	
	@Override
	public void update(BillJobTempView entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"update", this.getClass()});
		BillJobTempEntity  tempEntity = new BillJobTempEntity();
		tempEntity.setId(entity.getId());
		tempEntity.setAccountId(entity.getAcountId());
		tempEntity.setBankId(entity.getBankId());
		tempEntity.setBillDate(entity.getBillDate());
		tempEntity.setBillStartDate(entity.getBillStartDate());
		tempEntity.setBillEndDate(new Date());
		tempEntity.setBillType(entity.getBillType());
		tempEntity.setCardEndOfFour(entity.getCardEndOfFour());
		tempEntity.setIsVaild(entity.getIsVaild());
		String userEmail = entity.getUserEmail();
		tempEntity.setUserEmail(userEmail);
		billJobTempDao.save(tempEntity);
	}

	@Transactional(readOnly = true)
	@Override
	public boolean find(String card, String email, int billType) {
		//log.info("method:{} \tservice:{}", new Object[]{"find", this.getClass()});
		List<BillJobTempEntity> findBy = billJobTempDao.find(card, email, billType);
		return findBy.size() == 0 ? false : true;
	}
	
	@Override
	public void delete(Long id) {
		//log.info("method:{} \tservice:{}", new Object[]{"delete", this.getClass()});
		billJobTempDao.delete(id);
	}

	public BillJobTempDao getBillJobTempDao() {
		return billJobTempDao;
	}
	
	public void setBillJobTempDao(BillJobTempDao billJobTempDao) {
		this.billJobTempDao = billJobTempDao;
	}

}
