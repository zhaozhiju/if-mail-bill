package net.umpay.mailbill.service.impl.resolve;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;
import net.umpay.mailbill.api.resolve.IBillCycleInfoService;
import net.umpay.mailbill.hql.dao.BillCycleInfoDao;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.util.date.DateCal;
import net.umpay.mailbill.util.exception.MailBillException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮件账单周期表
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class BillCycleInfoServiceImpl implements IBillCycleInfoService {

	private static Logger log = LoggerFactory.getLogger(BillCycleInfoServiceImpl.class);
	
	@Autowired
	private BillCycleInfoDao billCycleInfoDao;
	@Autowired
	private NotBillCycleInfoServiceImpl notBillCycleInfoServiceImpl;
	
	@Override
	public void save(BillCycleInfoEntity entity) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"save", this.getClass()});
		billCycleInfoDao.save(entity);
	}

	@SuppressWarnings({ "deprecation"})
	@Transactional(readOnly = true)
	@Override
	public List<JspInfoPartView> findEntityByEmailUrl(String emailUrl) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByEmailUrl", this.getClass()});
		
		List<BillCycleInfoEntity> findMonthBy = billCycleInfoDao.findMonthByEmailUrl(emailUrl);
		List<BillCycleInfoEntity> findBy = billCycleInfoDao.findDayByEmailUrl(emailUrl);
		
		//非账单的查询后给页面展示
		List<JspInfoPartView> views = notBillCycleInfoServiceImpl.findEntityByEmailUrl(emailUrl);
		//账单的查询综合非账单信息后给页面展示
		for (int i = 0; i < findMonthBy.size(); i++){
			BillCycleInfoEntity entity = findMonthBy.get(i);
			JspInfoPartView infoView = new JspInfoPartView();
			Long id = entity.getId();
			String oldHtml = entity.getSubject();
			String newHtml = entity.getSentData().toLocaleString();
			String newDFS = entity.getNewHtmlDFS();
			String oldDFS = entity.getOldHtmlDFS();
			Integer bankBillType = entity.getBankBillType() == null ? 0 : entity.getBankBillType();
			Integer isBill = entity.getIsBill() == null ? -1 : entity.getIsBill();
			String accountOfDate = entity.getAccountOfDate();
			String cardEndOfFour = entity.getCardEndOfFour();
			
			infoView.setBankBillType(bankBillType);
			infoView.setId(id);
			infoView.setNewHtml(newHtml);
			infoView.setOldHtml(oldHtml);
			infoView.setCardEndOfFour(cardEndOfFour);
			infoView.setOldDFS(oldDFS);
			infoView.setNewDFS(newDFS);
			infoView.setIsBill(isBill);
			infoView.setAccountOfDate(accountOfDate);
			views.add(infoView);
		}
		for (int i = 0; i < findBy.size(); i++){
			BillCycleInfoEntity entity = findBy.get(i);
			JspInfoPartView infoView = new JspInfoPartView();
			Long id = entity.getId();
			String oldHtml = entity.getSubject();
			String newHtml = entity.getSentData().toLocaleString();
			String newDFS = entity.getNewHtmlDFS();
			String oldDFS = entity.getOldHtmlDFS();
			Integer bankBillType = entity.getBankBillType() == null ? 0 : entity.getBankBillType();
			Integer isBill = entity.getIsBill() == null ? -1 : entity.getIsBill();
			String accountOfDate = entity.getAccountOfDate();
			String cardEndOfFour = entity.getCardEndOfFour();
			
			infoView.setBankBillType(bankBillType);
			infoView.setId(id);
			infoView.setNewHtml(newHtml);
			infoView.setOldHtml(oldHtml);
			infoView.setCardEndOfFour(cardEndOfFour);
			infoView.setOldDFS(oldDFS);
			infoView.setNewDFS(newDFS);
			infoView.setIsBill(isBill);
			infoView.setAccountOfDate(accountOfDate);
			views.add(infoView);
		}
		
		return views;
	}
	
	@Transactional(readOnly = true)
	@Override
	public BillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findEntityByBillCyclePkId", this.getClass()});
		BillCycleInfoEntity findUniqueBy = billCycleInfoDao.findEntityByBillCyclePkId(billCyclePkId);
		return findUniqueBy;
	}
	
	@Transactional(readOnly = true)
	@Override
	public int existOfEmail(String oldhtml, String email) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"existOfEmail", this.getClass()});
		//根据拼接地址与邮箱地址从账单周期表内查询是否存在
		int existOfEmail = billCycleInfoDao.existOfEmail(oldhtml, email);
		//根据拼接地址与邮箱地址从非账单周期表内查询是否存在
		int existOfEmail2 = notBillCycleInfoServiceImpl.existOfEmail(oldhtml, email);
		//是否存在
		return existOfEmail+existOfEmail2;
	}
	
	@Transactional(readOnly = true)
	@Override
	public BillCycleInfoEntity getDFSURlName(String newhtml, String card) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"getDFSURlName", this.getClass()});
		BillCycleInfoEntity list = billCycleInfoDao.getDFSURlName(newhtml, card);
		return list;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<BillCycleInfoEntity> getDFSURlByNew(String newhtml) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"getDFSURlByNew", this.getClass()});
		List<BillCycleInfoEntity> findBy = billCycleInfoDao.getDFSURlByNew(newhtml);
		return findBy;
	}

	@Transactional(readOnly = true)
	@Override
	public List<BillCycleInfoEntity> findMonthExit(String userEmail, Date month, int bankId, int billDate,
			int billType, String cardEndOfFour) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findMonthExit", this.getClass()});
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		String format = sdf.format(date);
		List<BillCycleInfoEntity> findExit = billCycleInfoDao.findMonthExit(userEmail, format, bankId, billDate, billType, cardEndOfFour);
		return findExit;
	}
	
	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<BillCycleInfoEntity> findDayExit(String name, Date date, int bankId,
			int billType, String cardEndOfFour , int flag) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findDayExit", this.getClass()});
		Date date1 = new Date();
		DateCal dateCal = new DateCal(date1.toLocaleString());
		String decDay = dateCal.decDay(flag == 2 ? 2 : 1);
		decDay = decDay.replaceAll("-", "");
		List<BillCycleInfoEntity> findExit = billCycleInfoDao.findDayExit(name, decDay, bankId, billType, cardEndOfFour);
		return findExit;
	}

	@Transactional(readOnly = true)
	@Override
	public List<BillCycleInfoEntity> getInfo(String emailUrl) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"getInfo", this.getClass()});
		return billCycleInfoDao.getInfo(emailUrl);
	}
	
	@Transactional(readOnly = true)
	@Override
	public Map<String, String> findExistEmailCount(String string) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"findExistEmailCount", this.getClass()});
		// 非账单里面查询出来源为该邮箱的old拼接地址
		Map<String, String> map = notBillCycleInfoServiceImpl.findExistEmailCount(string);
		// 账单里面查询出来源为该邮箱的old拼接地址
		List<String> count = billCycleInfoDao.findExistEmailCount(string);
		for(String str : count){
			map.put(str, string);
		}
		// 汇总为一个集合
		return map;
	}

	public BillCycleInfoDao getBillCycleInfoDao() {
		return billCycleInfoDao;
	}

	public void setBillCycleInfoDao(BillCycleInfoDao billCycleInfoDao) {
		this.billCycleInfoDao = billCycleInfoDao;
	}

	@Override
	public List<BillCycleInfoEntity> findInfoByBIC(int bankId,
			String infoSource, String cardEndOfFour) throws MailBillException{
		return billCycleInfoDao.findInfoByInfoSource(bankId, infoSource, cardEndOfFour);
	}

	@Override
	public List<BillCycleInfoEntity> findInfoByBAC(int bankId, Long accountId,
			String cardEndOfFour) throws MailBillException{
		return billCycleInfoDao.findInfoByAccountId(bankId, accountId, cardEndOfFour);
	}

	@Override
	public Long findscVersionSEQ() throws MailBillException{
		return billCycleInfoDao.findscVersionSEQ();
	}
	
}
