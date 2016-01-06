package net.umpay.mailbill.service.impl.resolve;

import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.cmbbank.CmbNewBillView;
import net.umpay.mailbill.api.model.viewpart.CycleBillView;
import net.umpay.mailbill.api.resolve.IConvert;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.date.DateUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 招商新版
 * @author admin
 *
 */
@Service
public class CmbNewConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_NEW_CMB;
	}

	@Override
	public CmbNewBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		CmbNewBillView billVieNewBillView = new CmbNewBillView();
		CycleBillView billView =  new CycleBillView();
		String CardEndOfFour = billCycleInfoEntity.getCardEndOfFour();
		if (!StringUtils.isBlank(CardEndOfFour)){
			billView.setCardEndOfFour(CardEndOfFour);
		}
		String userName = billCycleInfoEntity.getUserName();
		if (!StringUtils.isBlank(userName)){
			billView.setUserName(userName);
		}
		String userGender = billCycleInfoEntity.getUserGender();
		if (!StringUtils.isBlank(userGender)){
			billView.setUserGender(userGender);
		}
		double newRmbBalance = billCycleInfoEntity.getNewRmbBalance();
		if (-1 != newRmbBalance){
			billView.setNewRmbBalance(Double.toString(newRmbBalance));
		}
		double newUsaBalance = billCycleInfoEntity.getNewUsaBalance();
		if (-1 != newUsaBalance){
			billView.setNewUsaBalance(Double.toString(newUsaBalance));
		}
		double minRmbPayment = billCycleInfoEntity.getMinRmbPayment();
		if (-1 != minRmbPayment){
			billView.setMinRmbPayment(Double.toString(minRmbPayment));
		}
		double minUsaPayment = billCycleInfoEntity.getMinUsaPayment();
		if (-1 != minUsaPayment){
			billView.setMinUsaPayment(Double.toString(minUsaPayment));
		}
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();
		if (!StringUtils.isBlank(accountOfDate)){
			billView.setAccountOfDate(accountOfDate);
		}
		Date paymentDueDate = billCycleInfoEntity.getPaymentDueDate();
		if (null != paymentDueDate){
			billView.setPaymentDueDate(DateUtil.getFormatDate(paymentDueDate));
		}
		Integer billDate = billCycleInfoEntity.getBillDate();
		if (null != billDate && 0 != billDate ){
			billView.setBillDate(Integer.toString(billDate));
		}
		billVieNewBillView.setBillView(billView);
		
		return billVieNewBillView;
	}
	
}
