package net.umpay.mailbill.service.impl.mailhandle;

import java.util.List;

import net.umpay.mailbill.api.mailhandle.IFindAllAccount;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 查询所有用户的用户名跟密码
 * 
 * @author admin
 *
 */
@Service
public class FindAllAccountImpl implements IFindAllAccount {
	private static Logger log = LoggerFactory.getLogger(FindAllAccountImpl.class);
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	
	
	@Override
	public List<BillUserInfoEntity> findAccount() {
		//log.info("method:{} \tservice:{}", new Object[]{"findAccount", this.getClass()});
		try {
			return billUserInfoServiceImpl.findAll();
		} catch (MailBillException e) {
			MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		}
		return null;
	}

}
