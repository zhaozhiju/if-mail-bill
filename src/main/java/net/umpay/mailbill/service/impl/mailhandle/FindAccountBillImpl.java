package net.umpay.mailbill.service.impl.mailhandle;

import java.util.ArrayList;
import java.util.List;

import net.umpay.mailbill.api.mailhandle.IFindAccountBill;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 查询某用户下绑定邮箱下的 {所有账单、是账单、非账单} 列表接口
 * 
 * @author admin
 */
@Service
public class FindAccountBillImpl implements IFindAccountBill {

	private static Logger log = LoggerFactory.getLogger(FindAccountBillImpl.class);
	
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	
	
	@Override
	public List<BillCycleInfoEntity> getAccountBill(Long account, int billType) {
		//log.info("method:{} \tservice:{}", new Object[]{"getAccountBill", this.getClass()});
		List<BillCycleInfoEntity> list = new ArrayList<BillCycleInfoEntity>();
		List<List<BillCycleInfoEntity>> listAll = new ArrayList<List<BillCycleInfoEntity>>();
		try {
			//根据用户表示查询用户的邮箱地址
			List<BillUserInfoEntity> findMailByAccount = billUserInfoServiceImpl.findMailByAccount(account);
			if (findMailByAccount.size() == 0){
				//log.info("method:{} \tservice:{} the user does not have binding mailbox！！！", new Object[]{"getAccountBill", this.getClass()});
				return null; 
			}else{
				//查询出多个账单列表
				for (BillUserInfoEntity entity : findMailByAccount){
					String emailUrl = entity.getEmailUrl();
					listAll.add(billCycleInfoServiceImpl.getInfo(emailUrl));
				}
				//将符合条件的数据，存放到一个账单列表
				int size = listAll.size();
				for (int i = 0; i < size; i++){
					for (BillCycleInfoEntity entity : listAll.get(i)){
						Integer isBill = entity.getIsBill();
						if (billType == isBill){
							list.add(entity);
						}
						if(billType != 1 && billType != 0 ){
							list.add(entity);
						}
					}
				}
				
			}
			return list; 
		} catch (MailBillException e) {
			MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		}
		return null;
	}

}
