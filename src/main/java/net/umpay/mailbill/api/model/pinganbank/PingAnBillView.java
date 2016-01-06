package net.umpay.mailbill.api.model.pinganbank;

import java.util.List;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.api.model.viewpart.BalanceBillView;
import net.umpay.mailbill.api.model.viewpart.CycleBillView;
import net.umpay.mailbill.api.model.viewpart.IntegrationBillView;
import net.umpay.mailbill.api.model.viewpart.MonthBillView;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

/**
 * 平安银行账单VO
 * @author admin
 *
 */
public class PingAnBillView extends BillTypeView{
	//账期信息
	CycleBillView billView;
	
	//账单还款汇总
	List<BalanceBillView> balanceBillViews;
	
	 //账单详细
	List<MonthBillView> monthBillViews;
	
	//积分信息
	List<IntegrationBillView> integrationBillView;
	
	@Override
	public int getVoBillType() {
		return MailBillTypeConstants.BILL_TYPE_PINGAN;
	}
	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_MONTH;
	}
	public CycleBillView getBillView() {
		return billView;
	}

	public void setBillView(CycleBillView billView) {
		this.billView = billView;
	}

	public List<BalanceBillView> getBalanceBillViews() {
		return balanceBillViews;
	}

	public void setBalanceBillViews(List<BalanceBillView> balanceBillViews) {
		this.balanceBillViews = balanceBillViews;
	}

	public List<MonthBillView> getMonthBillViews() {
		return monthBillViews;
	}

	public void setMonthBillViews(List<MonthBillView> monthBillViews) {
		this.monthBillViews = monthBillViews;
	}

	public List<IntegrationBillView> getIntegrationBillView() {
		return integrationBillView;
	}

	public void setIntegrationBillView(List<IntegrationBillView> integrationBillView) {
		this.integrationBillView = integrationBillView;
	}
}
