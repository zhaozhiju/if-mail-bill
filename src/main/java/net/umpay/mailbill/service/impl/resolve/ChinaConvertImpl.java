package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.chinabank.ChinaBillView;
import net.umpay.mailbill.api.model.viewpart.BalanceBillView;
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
 * 中国电子账单转换类
 * 
 * @author admin
 *
 */
@Service
public class ChinaConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_CHINA;
	}

	@Override
	public ChinaBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		ChinaBillView chinaBillView = new ChinaBillView();
		
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
		Integer billDate = billCycleInfoEntity.getBillDate();
		if (null != billDate && 0 != billDate){
			billView.setBillDate(Integer.toString(billDate));
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
		long usaIntegration = billCycleInfoEntity.getUsaIntegration();
		if (-1 != usaIntegration){
			billView.setUsaIntegration(Long.toString(usaIntegration));
		}
		long rmbIntegration = billCycleInfoEntity.getRmbIntegration();
		if (-1 != rmbIntegration){
			billView.setRmbIntegration(Long.toString(rmbIntegration));
		}
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();
		if (!StringUtils.isBlank(accountOfDate)){
			billView.setAccountOfDate(accountOfDate);
		}
		double rmbCreditLimit = billCycleInfoEntity.getRmbCreditLimit();
		if (-1 != rmbCreditLimit){
			billView.setRmbCreditLimit(Double.toString(rmbCreditLimit));
		}
		double usaCreditLimit = billCycleInfoEntity.getUsaCreditLimit();
		if (-1 != usaCreditLimit){
			billView.setUsaCreditLimit(Double.toString(usaCreditLimit));
		}
		Date paymentDueDate = billCycleInfoEntity.getPaymentDueDate();
		if (null != paymentDueDate){
			billView.setPaymentDueDate(DateUtil.getFormatDate(paymentDueDate));
		}
		double cashRmbAdvanceLimit = billCycleInfoEntity.getCashRmbAdvanceLimit();
		if (-1 != cashRmbAdvanceLimit){
			billView.setCashRmbAdvanceLimit(Double.toString(cashRmbAdvanceLimit));
		}
		double cashUsaAdvanceLimit = billCycleInfoEntity.getCashUsaAdvanceLimit();
		if (-1 != cashUsaAdvanceLimit){
			billView.setCashUsaAdvanceLimit(Double.toString(cashUsaAdvanceLimit));
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
			int incomeOrPay = bankMonthDetailEntity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				monthBillView.setIncomeOrPay(incomeOrPay);
			}
			int isMaster = bankMonthDetailEntity.getIsMaster();
			if (-1 != isMaster){
				monthBillView.setIsMaster(isMaster);
			}
			String merchandiseDetail = bankMonthDetailEntity.getMerchandiseDetail();
			if (!StringUtils.isBlank(merchandiseDetail)){
				monthBillView.setMerchandiseDetail(merchandiseDetail);
			}
			double amount = bankMonthDetailEntity.getAmount();
			if (-1 != amount){
				monthBillView.setAmount(amount);
			}
			double originalTransAmount = bankMonthDetailEntity.getOriginalTransAmount();
			if (-1 != originalTransAmount){
				monthBillView.setOriginalTransAmount(originalTransAmount);
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
		List<IntegrationBillView> integrationBillView = new ArrayList<IntegrationBillView>();
		for(IntegrationDetailEntity detailEntity : detailEntities){
			IntegrationBillView billView2 = new IntegrationBillView();
			int balancePoints = detailEntity.getBalancePoints();
			if (-1 != balancePoints){
				billView2.setBalancePoints(balancePoints);
			}
			int addedPoints = detailEntity.getAddedPoints();
			if (-1 != addedPoints){
				billView2.setAddedPoints(addedPoints);
			}
			int revisePoints = detailEntity.getRevisePoints();
			if (-1 != revisePoints){
				billView2.setRevisePoints(revisePoints);
			}
			int awardPoints = detailEntity.getAwardPoints();
			if (-1 != awardPoints){
				billView2.setAwardPoints(awardPoints);
			}
			int exchangePoints = detailEntity.getExchangePoints();
			if (-1 != exchangePoints){
				billView2.setExchangePoints(exchangePoints);
			}
			int usePoints = detailEntity.getUsePoints();
			if (-1 != usePoints){
				billView2.setUsePoints(usePoints);
			}
			String currencyType = detailEntity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				billView2.setCurrencyType(currencyType);
			}
			integrationBillView.add(billView2);
		}
		chinaBillView.setBillView(billView);
		chinaBillView.setBalanceBillViews(balanceBillViews);
		chinaBillView.setMonthBillViews(monthBillViews);
		chinaBillView.setIntegrationBillView(integrationBillView);
		return chinaBillView;
	}
	
}
