package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.cebbank.CebBillView;
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
 * 光大账单转换类
 * 
 * @author admin
 *
 */
@Service
public class CebConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_CEB;
	}

	@Override
	public CebBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		CebBillView cebBillView = new CebBillView();
		
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
		String accountOfDate = billCycleInfoEntity.getAccountOfDate();//账期
		if (!StringUtils.isBlank(accountOfDate)){
			billView.setAccountOfDate(accountOfDate);
		}
		double rmbCreditLimit = billCycleInfoEntity.getRmbCreditLimit();
		if (-1 != rmbCreditLimit){
			billView.setRmbCreditLimit(Double.toString(rmbCreditLimit));
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
			int incomeOrPay = bankMonthDetailEntity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				monthBillView.setIncomeOrPay(incomeOrPay);
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
		
		cebBillView.setBillView(billView);
		cebBillView.setIntegrationBillView(billView2);
		cebBillView.setMonthBillViews(monthBillViews);
		return cebBillView;
	}
	
}
