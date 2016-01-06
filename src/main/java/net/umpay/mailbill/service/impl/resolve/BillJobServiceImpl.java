package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.resolve.IBillJobService;
import net.umpay.mailbill.hql.dao.BillJobDao;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillJobEntity;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 邮件账单任务Service
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillJobServiceImpl implements IBillJobService {

	private static Logger log = LoggerFactory.getLogger(BillJobServiceImpl.class);
	
	@Autowired
	private BillJobDao billJobDao;
	
	/**
	 * 将符合条件的任务保存到表中
	 * 
	 * @param billCycleInfoEntity
	 */
	@Override
	public void saveJob(BillCycleInfoEntity billCycleInfoEntity) {
		//log.info("method:{} \tservice:{}", new Object[]{"saveJob", this.getClass()});
		Integer bankId = billCycleInfoEntity.getBankId();
		Integer billDate = billCycleInfoEntity.getBillDate();
		Integer billType = billCycleInfoEntity.getBillType();
		String cardEndOfFour = billCycleInfoEntity.getCardEndOfFour();
		String userEmail = billCycleInfoEntity.getInfoSource();
		if (null != billType && MailBillTypeConstants.BILL_TYPE_DAY == billType){
			//如果这些内容都不为空的话，进行下一步
			if (bankId != null && bankId != 0 && !StringUtils.isBlank(cardEndOfFour) 
					&& !StringUtils.isBlank(userEmail)){
				//如果返回false则说明任务表内没有此项内容
				boolean findJob = findDayJob(bankId, billType, cardEndOfFour, userEmail);
				//如果没有此项任务的话我们就需要添加
				if (!findJob){
					BillJobEntity entity = new BillJobEntity();
					entity.setBankId(bankId);
					entity.setBillType(billType);
					entity.setCardEndOfFour(cardEndOfFour);
					entity.setUserEmail(userEmail);
					billJobDao.save(entity);
				}
			}
		}else{
			//如果这些内容都不为空的话，进行下一步
			if (bankId != null && bankId != 0 &&  billDate != null && billDate != 0 && billType != null 
					&& billType == 1 && !StringUtils.isBlank(cardEndOfFour) 
					&& !StringUtils.isBlank(userEmail)){
				//如果返回false则说明任务表内没有此项内容
				boolean findJob = findJob(bankId, billDate, billType, cardEndOfFour, userEmail);
				//如果没有此项任务的话我们就需要添加
				if (!findJob){
					BillJobEntity entity = new BillJobEntity();
					entity.setBankId(bankId);
					entity.setBillDate(billDate);
					entity.setBillType(billType);
					entity.setCardEndOfFour(cardEndOfFour);
					entity.setUserEmail(userEmail);
					billJobDao.save(entity);
				}
			}
		}
	}
	
	@Transactional(readOnly = true)
	@SuppressWarnings("deprecation")
	@Override
	public List<BillJobEntity> findBillDateAndDay() {
		//log.info("method:{} \tservice:{}", new Object[]{"findBillDateAndDay", this.getClass()});
		Date date = new Date();
		int day = date.getDate();
		return billJobDao.findBillDateAndDay(day);
	}
	
	@Transactional(readOnly = true)
	@Override
	public boolean findJob(int bankId, int billDate, int billType, String cardEndOfFour, String userEmail) {
		//log.info("method:{} \tservice:{}", new Object[]{"findJob", this.getClass()});
		boolean findJob = billJobDao.findJob(bankId, billDate, billType, cardEndOfFour, userEmail);
		return findJob;
	}
	
	@Transactional(readOnly = true)
	@Override
	public boolean findDayJob(int bankId, int billType, String cardEndOfFour, String userEmail) {
		//log.info("method:{} \tservice:{}", new Object[]{"findDayJob", this.getClass()});
		boolean findJob = billJobDao.findDayJob(bankId, billType, cardEndOfFour, userEmail);
		return findJob;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillJobEntity> findBillTypeAndDay() {
		//log.info("method:{} \tservice:{}", new Object[]{"findBillTypeAndDay", this.getClass()});
		List<BillJobEntity> findBillTypeAndDay = billJobDao.findBillTypeAndDay();
		return findBillTypeAndDay;
	}

	public BillJobDao getBillJobDao() {
		return billJobDao;
	}

	public void setBillJobDao(BillJobDao billJobDao) {
		this.billJobDao = billJobDao;
	}

}
