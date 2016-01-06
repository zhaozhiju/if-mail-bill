package net.umpay.mailbill.api.model.cmbbank;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.api.model.viewpart.CycleBillView;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;

public class CmbNewBillView extends BillTypeView{

	CycleBillView billView ;
	
	@Override
	public int getVoBillType() {
		return MailBillTypeConstants.BILL_TYPE_NEW_CMB;
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
	
}
