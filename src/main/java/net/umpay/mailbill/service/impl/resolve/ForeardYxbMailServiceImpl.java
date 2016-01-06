package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.resolve.IForwardYxbMailService;
import net.umpay.mailbill.hql.dao.ForwardYxbMailDao;
import net.umpay.mailbill.hql.model.ForwardYxbMailEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * 自建邮箱服务Service
 * @author admin
 *
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)  
public class ForeardYxbMailServiceImpl implements IForwardYxbMailService {

	private static Logger log = LoggerFactory.getLogger(ForeardYxbMailServiceImpl.class);

	@Autowired
	private ForwardYxbMailDao dao;
	
	@Override
	public void saveForwardYxbMail(ForwardYxbMailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"addForwardYxbMail", this.getClass()});
		dao.save(entity);
	}

	@Override
	public void delectForwardYxbMail(ForwardYxbMailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"delectForwardYxbMail", this.getClass()});
		dao.delete(entity);
	}

	@Override
	public void updateForwardYxbMail(ForwardYxbMailEntity entity) {
		//log.info("method:{} \tservice:{}", new Object[]{"updateForwardYxbMail", this.getClass()});
		dao.save(entity);
	}

	@Override
	public List<ForwardYxbMailEntity> findForwardYxbMailByMailname(
			String mailname) {
		//log.info("method:{} \tservice:{}", new Object[]{"findForwardYxbMailByMailname", this.getClass()});
		return dao.findForwardYxbMailByMailname(mailname);
	}

	@Override
	public List<ForwardYxbMailEntity> findForwardYxbMailByYxbMailname(
			String yxbMailname) {
		//log.info("method:{} \tservice:{}", new Object[]{"findForwardYxbMailByYxbMailname", this.getClass()});
		return dao.findForwardYxbMailByYxbMailname(yxbMailname);
	}

	public ForwardYxbMailDao getDao() {
		return dao;
	}

	public void setDao(ForwardYxbMailDao dao) {
		this.dao = dao;
	}
	
}
