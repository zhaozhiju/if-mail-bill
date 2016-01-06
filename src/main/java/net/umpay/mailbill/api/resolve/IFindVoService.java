package net.umpay.mailbill.api.resolve;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.util.exception.MailBillException;

public interface IFindVoService {

	/**
	 * 根据主键与账单具体类型来获取转换后的View
	 * 
	 * @param billType 账单类型
	 * @param billCyclePkId	周期表的主键
	 * @return	相应的View
	 */
	public BillTypeView getView(int billType, Long billCyclePkId) throws MailBillException;
	
}
