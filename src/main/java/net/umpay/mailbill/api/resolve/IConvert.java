package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;

public interface IConvert {

	/**
	 * VO与实体之间的转换方法
	 * @param bankDayDetailEntities
	 * @param bankMonthDetailEntities
	 * @param detailEntities
	 * @param billCycleInfoEntity
	 * @return 返回相应VO
	 */
	public BillTypeView convertEntityAndView(List<BillBankDayDetailEntity> bankDayDetailEntities, 
			List<BillBankMonthDetailEntity> bankMonthDetailEntities, 
			List<IntegrationDetailEntity> detailEntities,
			List<BalanceDetailEntity> balanceDetailEntities,
			BillCycleInfoEntity  billCycleInfoEntity);
	
	/**
	 * 获取改邮件账单的详细转换类型
	 * 
	 * @return int
	 */
	public int getBillType();
}
