package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.cmbbank.CmbMonthlyBillView;
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
 * 招商商务卡VO与实体的转换类
 */
@Service
public class CmbMonthlyConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_MONTHLY_CMB;
	}

	@Override
	public CmbMonthlyBillView convertEntityAndView(List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		//VO总体的
		CmbMonthlyBillView billView = new CmbMonthlyBillView();
		
		CycleBillView  cycleBillView = new CycleBillView();
		String CardEndOfFour = billCycleInfoEntity.getCardEndOfFour();
		if (StringUtils.isNotBlank(CardEndOfFour)){
			cycleBillView.setCardEndOfFour(CardEndOfFour);
		}
		String userName = billCycleInfoEntity.getUserName();
		if (StringUtils.isNotBlank(userName)){
			cycleBillView.setUserName(userName);
		}
		String userGender = billCycleInfoEntity.getUserGender();
		if (StringUtils.isNotBlank(userGender)){
			cycleBillView.setUserGender(userGender);
		}
		Integer billDate = billCycleInfoEntity.getBillDate();
		if (null != billDate && 0 != billDate){
			cycleBillView.setBillDate(Integer.toString(billDate));
		}
		// 账期
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();
		if (StringUtils.isNotBlank(accountOfDate)) {
			cycleBillView.setAccountOfDate(accountOfDate);
		}
		Date paymentDueDate = billCycleInfoEntity.getPaymentDueDate();
		if (paymentDueDate != null){
			cycleBillView.setPaymentDueDate(DateUtil.getFormatDate(paymentDueDate));
		}
		double rmbCreditLimit = billCycleInfoEntity.getRmbCreditLimit();
		if (0 != rmbCreditLimit){
			cycleBillView.setRmbCreditLimit(Double.toString(rmbCreditLimit));
		}
		long rmbIntegration = billCycleInfoEntity.getRmbIntegration();
		if (0 != rmbIntegration){
			cycleBillView.setRmbIntegration(Long.toString(rmbIntegration));
		}
		double usaCreditLimit = billCycleInfoEntity.getUsaCreditLimit();
		if (0 != usaCreditLimit){
			cycleBillView.setUsaCreditLimit(Double.toString(usaCreditLimit));
		}
		double newRmbBalance = billCycleInfoEntity.getNewRmbBalance();
		if (0 != newRmbBalance){
			cycleBillView.setNewRmbBalance(Double.toString(newRmbBalance));
		}
		double newUsaBalance = billCycleInfoEntity.getNewUsaBalance();
		if (0 != newUsaBalance){
			cycleBillView.setNewUsaBalance(Double.toString(newUsaBalance));
		}

		List<BalanceBillView> balanceBillViews = new ArrayList<BalanceBillView>();
		for (BalanceDetailEntity detailEntity : balanceDetailEntities){
			BalanceBillView balanceBillView = new BalanceBillView();
			double balance = detailEntity.getBalance();
			if (-1 != balance){
				balanceBillView.setBalance(balance);
			}
			double newCharges = detailEntity.getNewCharges();
			if (-1 != newCharges){
				balanceBillView.setNewCharges(newCharges);
			}
			double payment = detailEntity.getPayment();
			if (-1 != payment){
				balanceBillView.setPayment(payment);
			}
			double adjustment = detailEntity.getAdjustment();
			if (-1 != adjustment){
				balanceBillView.setAdjustment(adjustment);
			}
			double interest = detailEntity.getInterest();
			if (-1 != interest){
				balanceBillView.setInterest(interest);
			}
			double newBalance = detailEntity.getNewBalance();
			if (-1 != newBalance){
				balanceBillView.setNewBalance(newBalance);
			}
			String currencyType = detailEntity.getCurrencyType();
			if (StringUtils.isNotBlank(currencyType)){
				balanceBillView.setCurrencyType(currencyType);
			}
			balanceBillViews.add(balanceBillView);
		}
		
		List<MonthBillView> billViews = new ArrayList<MonthBillView>();
		for (BillBankMonthDetailEntity entity : bankMonthDetailEntities){
			MonthBillView monthBillView = new MonthBillView();
			
			Date merchandiseDate = entity.getMerchandiseDate();
			if (null != merchandiseDate ){
				monthBillView.setMerchandiseDate(DateUtil.getFormatDate(merchandiseDate));
			}
			Date postDate = entity.getPostDate();
			if (null != postDate){
				monthBillView.setPostDate(DateUtil.getFormatDate(postDate));
			}
			int isMaster = entity.getIsMaster();
			if (-1 != isMaster){
				monthBillView.setIsMaster(isMaster);
			}
			int incomeOrPay = entity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				monthBillView.setIncomeOrPay(incomeOrPay);
			}
			String merchandiseDetail = entity.getMerchandiseDetail();
			if (StringUtils.isNotBlank(merchandiseDetail)){
				monthBillView.setMerchandiseDetail(merchandiseDetail);
			}
			double amount = entity.getAmount();
			if (-1 != amount){
				monthBillView.setAmount(amount);
			}
			String currencyType = entity.getCurrencyType();
			if (StringUtils.isNotBlank(currencyType)){
				monthBillView.setCurrencyType(currencyType);
			}
			String cardEndOfFour = entity.getCardEndOfFour();
			if (StringUtils.isNotBlank(cardEndOfFour)){
				monthBillView.setCardEndOfFour(cardEndOfFour);
			}
			String merchandiseArea = entity.getMerchandiseArea();
			if (StringUtils.isNotBlank(merchandiseArea)){
				monthBillView.setMerchandiseArea(merchandiseArea);
			}
			double originalTransAmount = entity.getOriginalTransAmount();
			if (-1 != originalTransAmount){
				monthBillView.setOriginalTransAmount(originalTransAmount);
			}
			billViews.add(monthBillView);
		}
		
		List<IntegrationBillView> billView2 = new ArrayList<IntegrationBillView>();
		for (IntegrationDetailEntity detailEntity : detailEntities){
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
			if (StringUtils.isNotBlank(currencyType)){
				integrationBillView.setCurrencyType(currencyType);
			}
			billView2.add(integrationBillView);
		}
		billView.setBillView(cycleBillView);
		billView.setBalanceBillViews(balanceBillViews);
		billView.setIntegrationBillView(billView2);
		billView.setMonthBillViews(billViews);
		return billView;
	}
	
	
}
