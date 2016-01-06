package net.umpay.mailbill.service.impl.job;

import java.util.ArrayList;
import java.util.List;

import net.umpay.mailbill.api.job.IFindBillDateSame;
import net.umpay.mailbill.api.model.job.BillJobView;
import net.umpay.mailbill.api.objectmapper.IMailBillObjectMapper;
import net.umpay.mailbill.hql.model.BillJobEntity;
import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillJobServiceImpl;
import net.umpay.mailbill.service.impl.resolve.BillUserInfoServiceImpl;
import net.umpay.mailbill.util.exception.MailBillException;

import org.apache.commons.lang.StringUtils;
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
public class FindBillDateSameImpl implements IFindBillDateSame {
	
	private static Logger log = LoggerFactory.getLogger(FindBillDateSameImpl.class);
	
	@Autowired
	private BillUserInfoServiceImpl billUserInfoServiceImpl;
	
	@Autowired
	private BillJobServiceImpl billJobServiceImpl;
	@Autowired
	private IMailBillObjectMapper mailBillObjectMapper;
	
	@Override
	public List<BillJobView> findDateSame() {
		
		List<BillJobEntity> findBillDateAndDay = billJobServiceImpl.findBillDateAndDay();
		List<BillJobView> billJobViews = new ArrayList<BillJobView>();
		String userEmail = null;
		BillUserInfoEntity findUser = null;
		for (BillJobEntity billJobEntity : findBillDateAndDay){
			findUser = null;
			BillJobView billJobView = new BillJobView();
			userEmail = billJobEntity.getUserEmail();
			findUser = billUserInfoServiceImpl.findUser(userEmail);
			if (findUser != null){
				billJobView.setPassword(findUser.getPassword());
				billJobView.setAccountId(findUser.getAccountId());
			}
			billJobView.setId(billJobEntity.getId());
			billJobView.setBankId(billJobEntity.getBankId());
			billJobView.setBillDate(billJobEntity.getBillDate());
			billJobView.setBillType(billJobEntity.getBillType());
			billJobView.setCardEndOfFour(billJobEntity.getCardEndOfFour());
			billJobView.setUserEmail(userEmail);
			billJobViews.add(billJobView);
		}
		
		try {
			log.info(
					"action:{} \t账单日为今天,用户邮箱及密码:{}",
					new Object[] {
							"findDateSame()",
							mailBillObjectMapper
									.writeValueAsString(billJobViews) });
		} catch (MailBillException me) {
			log.error(me.getMessage(), me);
			me.printStackTrace();
		}
		
		return billJobViews;
	}

	@Override
	public List<BillJobView> findBillTypeAndDay() {
		//log.info("method:{} \tservice:{}", new Object[]{"findBillTypeAndDay", this.getClass()});
		List<BillJobEntity> findBillTypeAndDay = billJobServiceImpl.findBillTypeAndDay();
		List<BillJobView> billJobViews = new ArrayList<BillJobView>();
		BillUserInfoEntity findUser = null;
		for (BillJobEntity billJobEntity : findBillTypeAndDay){
			findUser = null;
			BillJobView billJobView = new BillJobView();
			billJobView.setId(billJobEntity.getId());
			billJobView.setBankId(billJobEntity.getBankId());
			billJobView.setBillDate(billJobEntity.getBillDate());
			billJobView.setBillType(billJobEntity.getBillType());
			billJobView.setCardEndOfFour(billJobEntity.getCardEndOfFour());
			String userEmail = billJobEntity.getUserEmail();
			billJobView.setUserEmail(userEmail);
			if (!StringUtils.isBlank(userEmail)){
				 findUser = billUserInfoServiceImpl.findUser(userEmail);
				 billJobView.setAccountId(findUser.getAccountId());
				 billJobView.setPassword(findUser.getPassword());
			}
			billJobViews.add(billJobView);
		}
		return billJobViews;
	}

}
