package net.umpay.mailbill.service.impl.mailhandle;

import java.util.List;

import net.umpay.mailbill.api.mailhandle.IFindMail;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 查询某用户下绑定的邮箱列表接口
 * 
 * @author admin
 *
 */
@Service
public class FindMailImpl implements IFindMail {
	private static Logger log = LoggerFactory.getLogger(FindMailImpl.class);
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	
	
	@Override
	public List<BillUserInfoEntity> findMailByAccount(Long account) {
		//log.info("method:{} \tservice:{}", new Object[]{"findMailByAccount", this.getClass()});
		try {
			return billUserInfoServiceImpl.findMailByAccount(account);
		} catch (MailBillException e) {
			MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		}
		return null;
	}

}
