package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.ccbbank.CcbBillView;
import net.umpay.mailbill.api.model.viewpart.CycleBillView;
import net.umpay.mailbill.api.model.viewpart.IntegrationBillView;
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
 * 建行账单转换类
 */
@Service
public class CcbConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_CCB;
	}

	@Override
	public CcbBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		CcbBillView ccbBillView = new CcbBillView();
		
		CycleBillView billView = new CycleBillView();
		String cardEndOfFour1 = billCycleInfoEntity.getCardEndOfFour();
		if (!StringUtils.isBlank(cardEndOfFour1)){
			billView.setCardEndOfFour(cardEndOfFour1);
		}
		String userName = billCycleInfoEntity.getUserName();
		if (!StringUtils.isBlank(userName)){
			billView.setUserName(userName);
		}
		String userGender = billCycleInfoEntity.getUserGender();
		if (!StringUtils.isBlank(userGender)){
			billView.setUserGender(userGender);
		}
		Integer billDate = billCycleInfoEntity.getBillDate();
		if (null != billDate && 0 != billDate){
			billView.setBillDate(Integer.toString(billDate));
		}
		double rmbCreditLimit = billCycleInfoEntity.getRmbCreditLimit();
		if (-1 != rmbCreditLimit){
			billView.setRmbCreditLimit(Double.toString(rmbCreditLimit));
		}
		// 账期
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();
		if (StringUtils.isNotBlank(accountOfDate)){
			billView.setAccountOfDate(accountOfDate);
		}
		Date paymentDueDate = billCycleInfoEntity.getPaymentDueDate();
		if (null != paymentDueDate){
			billView.setPaymentDueDate(DateUtil.getFormatDate(paymentDueDate));
		}
		double cashRmbAdvanceLimit = billCycleInfoEntity.getCashRmbAdvanceLimit();
		if (-1 != cashRmbAdvanceLimit){
			billView.setCashRmbAdvanceLimit(Double.toString(cashRmbAdvanceLimit));
		}
		long rmbIntegration = billCycleInfoEntity.getRmbIntegration();
		if (-1 != rmbIntegration){
			billView.setRmbIntegration(Long.toString(rmbIntegration));
		}
		double minRmbPayment = billCycleInfoEntity.getMinRmbPayment();
		if (-1 != minRmbPayment){
			billView.setMinRmbPayment(Double.toString(minRmbPayment));
		}
		double newRmbBalance = billCycleInfoEntity.getNewRmbBalance();
		if (-1 != newRmbBalance){
			billView.setNewRmbBalance(Double.toString(newRmbBalance));
		}

		List<MonthBillView> monthBillViews = new ArrayList<MonthBillView>();
		for (BillBankMonthDetailEntity bankMonthDetailEntity: bankMonthDetailEntities){
			MonthBillView monthBillView = new MonthBillView();
			Date merchandiseDate = bankMonthDetailEntity.getMerchandiseDate();
			if (null != merchandiseDate){
				monthBillView.setMerchandiseDate(DateUtil.getFormatDate(merchandiseDate));
			}
			int isMaster = bankMonthDetailEntity.getIsMaster();
			if (-1 != isMaster){
				monthBillView.setIsMaster(isMaster);
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
			String cardEndOfFour = bankMonthDetailEntity.getCardEndOfFour();
			if (!StringUtils.isBlank(cardEndOfFour)){
				monthBillView.setCardEndOfFour(cardEndOfFour);
			}
			String currencyType = bankMonthDetailEntity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				monthBillView.setCurrencyType(currencyType);
			}
			int incomeOrPay = bankMonthDetailEntity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				monthBillView.setIncomeOrPay(incomeOrPay);
			}
			monthBillViews.add(monthBillView);
		}
		
		List<IntegrationBillView> billView2 = new ArrayList<IntegrationBillView>();
		for(IntegrationDetailEntity detailEntity : detailEntities){
			IntegrationBillView integrationBillView = new IntegrationBillView();
			int balancePoints = detailEntity.getBalancePoints();
			if (-1 != balancePoints){
				integrationBillView.setBalancePoints(balancePoints);
			}
			int addedPoints = detailEntity.getAddedPoints();
			if (-1 != addedPoints){
				integrationBillView.setAddedPoints(addedPoints);
			}
			int revisePoints = detailEntity.getRevisePoints();
			if (-1 != revisePoints){
				integrationBillView.setRevisePoints(revisePoints);
			}
			int awardPoints = detailEntity.getAwardPoints();
			if (-1 != awardPoints){
				integrationBillView.setAwardPoints(awardPoints);
			}
			int exchangePoints = detailEntity.getExchangePoints();
			if (-1 != exchangePoints){
				integrationBillView.setExchangePoints(exchangePoints);
			}
			int usePoints = detailEntity.getUsePoints();
			if (-1 != usePoints){
				integrationBillView.setUsePoints(usePoints);
			}
			String currencyType = detailEntity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				integrationBillView.setCurrencyType(currencyType);
			}
			billView2.add(integrationBillView);
		}
		
		/*List<BalanceBillView> balanceBillViews = new ArrayList<BalanceBillView>();
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
			double adjustment = balanceDetailEntity.getAdjustment();
			if (-1 != adjustment){
				balanceBillView.setAdjustment(adjustment);
			}
			double interest = balanceDetailEntity.getInterest();
			if (-1 != interest){
				balanceBillView.setInterest(interest);
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
		}*/
		
		ccbBillView.setBillView(billView);
		ccbBillView.setIntegrationBillView(billView2);
		ccbBillView.setMonthBillViews(monthBillViews);
//		ccbBillView.setBalanceBillViews(balanceBillViews);
		return ccbBillView;
	}
	
}
