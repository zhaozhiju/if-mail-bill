package net.umpay.mailbill.service.impl.resolve;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.umpay.mailbill.api.model.cmbbank.CmbDayBillView;
import net.umpay.mailbill.api.model.viewpart.CycleBillDayView;
import net.umpay.mailbill.api.model.viewpart.DayBillView;
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
 * 招行日账单PO2VO
 * @author admin
 *
 */
@Service
public class CmbDayConvertImpl implements IConvert{

	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_DAY_CMB;
	}
	
	@Override
	public CmbDayBillView convertEntityAndView(
			List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity) {
		
		CmbDayBillView dayBillView = new CmbDayBillView();
		
		CycleBillDayView billView = new CycleBillDayView();
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
		double rmbCreditLimit = billCycleInfoEntity.getRmbCreditLimit();
		if (rmbCreditLimit != 0){
			billView.setRmbCreditLimit(Double.toString(rmbCreditLimit));
		}
		double cashRmbAdvanceLimit = billCycleInfoEntity.getCashRmbAdvanceLimit();
		if (cashRmbAdvanceLimit != 0){
			billView.setCashRmbAdvanceLimit(Double.toString(cashRmbAdvanceLimit));
		}
		
		List<DayBillView> dayList = new ArrayList<DayBillView>();
		for (BillBankDayDetailEntity entity : bankDayDetailEntities){
			DayBillView day = new DayBillView();
			String cardEndOfFour = entity.getCardEndOfFour();
			if (!StringUtils.isBlank(cardEndOfFour)){
				day.setCardEndOfFour(cardEndOfFour);
			}
			int incomeOrPay = entity.getIncomeOrPay();
			if (-1 != incomeOrPay){
				day.setIncomeOrPay(incomeOrPay);
			}
			
			double merchandiseAmount = entity.getMerchandiseAmount();
			day.setMerchandiseAmount(String.valueOf(merchandiseAmount));
			
			String currencyType = entity.getCurrencyType();
			if (!StringUtils.isBlank(currencyType)){
				day.setCurrencyType(currencyType);
			}
			
			Date merchandiseDate = entity.getMerchandiseDate();
			if (merchandiseDate != null){
				day.setMerchandiseDate(DateUtil.getFormatDate(merchandiseDate));
			}
			
			String merchandiseDetail = entity.getMerchandiseDetail();
			if (!StringUtils.isBlank(merchandiseDetail)){
				day.setMerchandiseDetail(merchandiseDetail);
			}
			
			Date merchandiseTime = entity.getMerchandiseTime();
			if (null != merchandiseTime){
				day.setMerchandiseTime(DateUtil.getFormatDate(merchandiseTime, "HH:mm:ss"));
			}
			
			dayList.add(day);
		}
		
		dayBillView.setBillView(dayList);
		dayBillView.setCycleBillView(billView);
		return dayBillView;
	}

}
