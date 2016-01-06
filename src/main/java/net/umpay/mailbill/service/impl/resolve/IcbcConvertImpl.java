package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.icbcbank.IcbcBillView;
import net.umpay.mailbill.api.model.viewpart.BalanceBillView;
import net.umpay.mailbill.api.model.viewpart.CycleBillView;
import net.umpay.mailbill.api.model.viewpart.MonthBillView;
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
 * 工商账单转换类
 * 
 * @author admin
 *
 */
@Service
public class IcbcConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_ICBC;
	}

	@Override
	public IcbcBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		IcbcBillView icbcBillView = new IcbcBillView();
		CycleBillView billView = new CycleBillView();
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
		Date paymentDueDate = billCycleInfoEntity.getPaymentDueDate();
		if (null != paymentDueDate){
			billView.setPaymentDueDate(DateUtil.getFormatDate(paymentDueDate));
		}
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();
		if (!StringUtils.isBlank(accountOfDate)){
			billView.setAccountOfDate(accountOfDate);
		}
		long rmbIntegration = billCycleInfoEntity.getRmbIntegration();
		if (-1 != rmbIntegration){
			billView.setRmbIntegration(Long.toString(rmbIntegration));
		}
		double newRmbBalance = billCycleInfoEntity.getNewRmbBalance();
		if (0 <= newRmbBalance){
			billView.setNewRmbBalance(Double.toString(newRmbBalance));
		}
		double newUsaBalance = billCycleInfoEntity.getNewUsaBalance();
		if (0 <= newUsaBalance){
			billView.setNewUsaBalance(Double.toString(newUsaBalance));
		}
		Integer billDate = billCycleInfoEntity.getBillDate();
		if (null != billDate && 0 != billDate){
			billView.setBillDate(Integer.toString(billDate));
		}
			
		List<BalanceBillView> balanceBillViews = new ArrayList<BalanceBillView>();
		for (BalanceDetailEntity balanceDetailEntity : balanceDetailEntities){
			BalanceBillView balanceBillView = new BalanceBillView();
			double balance = balanceDetailEntity.getBalance();
			if (-1 != balance){
				balanceBillView.setBalance(balance);
			}
			double newCharges = balanceDetailEntity.getNewCharges();
			if (-1 != newCharges){
				balanceBillView.setNewCharges(newCharges);
			}
			double payment = balanceDetailEntity.getPayment();
			if (-1 != payment){
				balanceBillView.setPayment(payment);
			}
			double newBalance = balanceDetailEntity.getNewBalance();
			if (-1 != newBalance){
				balanceBillView.setNewBalance(newBalance);
			}
			String currencyType = balanceDetailEntity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				balanceBillView.setCurrencyType(currencyType);
			}
			balanceBillViews.add(balanceBillView);
		}
		
		List<MonthBillView> monthBillViews = new ArrayList<MonthBillView>();
		for (BillBankMonthDetailEntity bankMonthDetailEntity: bankMonthDetailEntities){
			MonthBillView monthBillView = new MonthBillView();
			Date merchandiseDate = bankMonthDetailEntity.getMerchandiseDate();
			if (null != merchandiseDate){
				monthBillView.setMerchandiseDate(DateUtil.getFormatDate(merchandiseDate));
			}
			Date postDate = bankMonthDetailEntity.getPostDate();
			if (null != postDate){
				monthBillView.setPostDate(DateUtil.getFormatDate(postDate));
			}
			String merchandiseDetail = bankMonthDetailEntity.getMerchandiseDetail();
			if (!StringUtils.isBlank(merchandiseDetail)){
				monthBillView.setMerchandiseDetail(merchandiseDetail);
			}
			double amount = bankMonthDetailEntity.getAmount();
			if (-1 != amount){
				monthBillView.setAmount(amount);
			}
			int isMaster = bankMonthDetailEntity.getIsMaster();
			if (-1 != isMaster){
				monthBillView.setIsMaster(isMaster);
			}
			int incomeOrPay = bankMonthDetailEntity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				monthBillView.setIncomeOrPay(incomeOrPay);
			}
			String cardEndOfFour = bankMonthDetailEntity.getCardEndOfFour();
			if (!StringUtils.isBlank(cardEndOfFour)){
				monthBillView.setCardEndOfFour(cardEndOfFour);
			}
			String currencyType = bankMonthDetailEntity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				monthBillView.setCurrencyType(currencyType);
			}
			monthBillViews.add(monthBillView);
		}
		
		icbcBillView.setBillView(billView);
		icbcBillView.setBalanceBillViews(balanceBillViews);
		icbcBillView.setMonthBillViews(monthBillViews);
		return icbcBillView;
	}
	
}
