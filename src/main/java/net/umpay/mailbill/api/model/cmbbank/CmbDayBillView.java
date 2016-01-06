package net.umpay.mailbill.api.model.cmbbank;

import java.util.List;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.api.model.viewpart.CycleBillDayView;
import net.umpay.mailbill.api.model.viewpart.DayBillView;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

/**
 * 日账单
 * @author admin
 *
 */
public class CmbDayBillView extends BillTypeView{

	//账期信息
	CycleBillDayView cycleBillView;
	
	List<DayBillView> billView;
	
	@Override
	public int getVoBillType() {
		return MailBillTypeConstants.BILL_TYPE_DAY_CMB;
	}
	@Override
	public int getBillType() {
		return MailBillTypeConstants.BILL_TYPE_DAY;
	}
	public List<DayBillView> getBillView() {
		return billView;
	}

	public void setBillView(List<DayBillView> billView) {
		this.billView = billView;
	}
	public CycleBillDayView getCycleBillView() {
		return cycleBillView;
	}
	public void setCycleBillView(CycleBillDayView cycleBillView) {
		this.cycleBillView = cycleBillView;
	}


}
