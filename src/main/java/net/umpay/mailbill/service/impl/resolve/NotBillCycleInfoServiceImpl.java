package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;
import net.umpay.mailbill.api.resolve.INotBillCycleInfoService;
import net.umpay.mailbill.hql.dao.NotBillCycleInfoDao;
import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;
import net.umpay.mailbill.util.exception.MailBillException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮件账单周期表(非账单)
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class NotBillCycleInfoServiceImpl implements INotBillCycleInfoService {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(NotBillCycleInfoServiceImpl.class);

	@Autowired
	private NotBillCycleInfoDao notBill;

	@Override
	public void save(NotBillCycleInfoEntity entity) throws MailBillException {
		//log.info("method:{} \tservice:{}",new Object[] { "save", this.getClass() });
		notBill.save(entity);
	}

	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public List<JspInfoPartView> findEntityByEmailUrl(String emailUrl)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] {"findEntityByEmailUrl", this.getClass() });
		List<NotBillCycleInfoEntity> findBy = notBill.findByEmailUrl(emailUrl);
		List<JspInfoPartView> views = new ArrayList<JspInfoPartView>();
		for (NotBillCycleInfoEntity entity : findBy) {
			JspInfoPartView infoView = new JspInfoPartView();
			Long id = entity.getId();
			String oldHtml = entity.getSubject();
			String newHtml = entity.getSentData().toLocaleString();
			String newDFS = entity.getNewHtmlDFS();
			String oldDFS = entity.getOldHtmlDFS();
			Integer isBill = entity.getIsBill() == null ? -1 : entity.getIsBill();

			infoView.setId(id);
			infoView.setNewHtml(newHtml);
			infoView.setOldHtml(oldHtml);
			infoView.setOldDFS(oldDFS);
			infoView.setNewDFS(newDFS);
			infoView.setIsBill(isBill);
			views.add(infoView);
		}
		return views;
	}

	@Transactional(readOnly = true)
	@Override
	public NotBillCycleInfoEntity getDFSURlName(String newhtml, String card)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] { "getDFSURlName", this.getClass() });
		NotBillCycleInfoEntity list = notBill.getDFSURlName(newhtml, card);
		return list;
	}

	@Transactional(readOnly = true)
	@Override
	public List<NotBillCycleInfoEntity> getInfo(String emailUrl)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] { "getInfo", this.getClass() });
		return notBill.getInfo(emailUrl);
	}

	@Transactional(readOnly = true)
	@Override
	public Map<String, String> findExistEmailCount(String string)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] {"findExistEmailCount", this.getClass() });
		Map<String, String> map = new HashMap<String, String>();
		List<String> count = notBill.findExistEmailCount(string);
		for (String str : count) {
			map.put(str, string);
		}
		return map;
	}

	@Transactional(readOnly = true)
	@Override
	public List<NotBillCycleInfoEntity> getDFSURlByNew(String newhtml)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] { "getDFSURlByNew", this.getClass() });
		List<NotBillCycleInfoEntity> findBy = notBill.getDFSURlByNew(newhtml);
		return findBy;
	}

	@Transactional(readOnly = true)
	@Override
	public int existOfEmail(String oldhtml, String email)
			throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[] { "existOfEmail", this.getClass() });
		int existOfEmail = notBill.existOfEmail(oldhtml, email);
		return existOfEmail;
	}

	public NotBillCycleInfoDao getNotBill() {
		return notBill;
	}

	public void setNotBill(NotBillCycleInfoDao notBill) {
		this.notBill = notBill;
	}
}
