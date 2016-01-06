package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.resolve.IBillCardInfoService;
import net.umpay.mailbill.hql.dao.BillCardInfoDao;
import net.umpay.mailbill.hql.model.BillCardInfoEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BillCardInfoServiceImpl implements IBillCardInfoService {
	private static Logger log = LoggerFactory.getLogger(BillCardInfoServiceImpl.class);

	@Autowired
	private BillCardInfoDao billCardInfoDao;
	
	@Transactional(readOnly = true)
	@Override
	public List<BillCardInfoEntity> find(int bankId, long accountId, String mainCardOfFour) {
		//log.info("method:{} \tservice:{}", new Object[]{"find", this.getClass()});
		List<BillCardInfoEntity> findInfo = billCardInfoDao.findInfo(bankId, accountId, mainCardOfFour);
		return findInfo;
	}
	
	@Override
	public int setBillDate(Integer bankId, Long accountId,
			String infoSourceEmail, String mainCardOfFour, Integer billDate) {
		//log.info("method:{} \tservice:{}", new Object[]{"setBillDate", this.getClass()});
		return billCardInfoDao.updateBillDate(billDate, bankId, accountId, infoSourceEmail, mainCardOfFour);
	}

	@Override
	public void save(BillCardInfoEntity billCardInfoEntity) {
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billCardInfoDao.save(billCardInfoEntity);
	}

	@Override
	public int setPaymentDueDate(Integer bankId, Long accountId,
			String infoSourceEmail, String mainCardOfFour, Date paymentDueDate) {
		//log.info("method:{} \tservice:{}", new Object[]{"setPaymentDueDate", this.getClass()});
		return billCardInfoDao.updatePaymentDueDate(paymentDueDate, bankId, accountId, infoSourceEmail, mainCardOfFour);
	}

	@Override
	public int setCardType(Integer bankId, Long accountId,
			String infoSourceEmail, String mainCardOfFour, String cardType) {
		//log.info("method:{} \tservice:{}", new Object[]{"setCardType", this.getClass()});
		return billCardInfoDao.updateCardType(cardType, bankId, accountId, infoSourceEmail, mainCardOfFour);
		
	}

}
