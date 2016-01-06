package net.umpay.mailbill.service.impl.resolve;

import java.util.List;

import net.umpay.mailbill.api.model.BillTypeView;
import net.umpay.mailbill.api.resolve.IConvert;
import net.umpay.mailbill.api.resolve.IFindVoService;
import net.umpay.mailbill.hql.model.BalanceDetailEntity;
import net.umpay.mailbill.hql.model.BillBankDayDetailEntity;
import net.umpay.mailbill.hql.model.BillBankMonthDetailEntity;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.IntegrationDetailEntity;
import net.umpay.mailbill.util.exception.MailBillException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FindVoServiceImpl implements IFindVoService{
	private static Logger log = LoggerFactory.getLogger(FindVoServiceImpl.class);
	
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private BillBankDayDetailServiceImpl bankDaDetailServiceImpl;
	@Autowired
	private BillBankMonthDetailServiceImpl bankMonthDetailServiceImpl;
	@Autowired
	private IntegrationDetailServiceImpl detailServiceImpl;
	@Autowired
	private BalanceDetailServiceImpl balanceDetailServiceImpl;
	
	public List<IConvert>  billTypeList;
	
	public List<IConvert> getBillTypeList() {
		return billTypeList;
	}
	public void setBillTypeList(List<IConvert> billTypeList) {
		this.billTypeList = billTypeList;
	}
	
	@Override
	public BillTypeView getView(int voBillType, Long billCyclePkId) throws MailBillException{
		//log.info("method:{} \tservice:{}", new Object[]{"getView", this.getClass()});
		BillCycleInfoEntity billCycleInfoEntity = billCycleInfoServiceImpl.findEntityByBillCyclePkId(billCyclePkId);
		List<BillBankDayDetailEntity> findDayDetail = bankDaDetailServiceImpl.findEntityByBillCyclePkId(billCyclePkId);
		List<BillBankMonthDetailEntity> findMonthDetail = bankMonthDetailServiceImpl.findEntityByBillCyclePkId(billCyclePkId);
		List<IntegrationDetailEntity> findIntegrationDetail = detailServiceImpl.findEntityByBillCyclePkId(billCyclePkId);
		List<BalanceDetailEntity> detailEntities = balanceDetailServiceImpl.findEntityByBillCyclePkId(billCyclePkId);
		if (0 != voBillType && -1 != voBillType) {
		  for (IConvert service : billTypeList) {
			  if (voBillType == service.getBillType()) { 
				  return service.convertEntityAndView(findDayDetail, findMonthDetail, findIntegrationDetail, detailEntities, billCycleInfoEntity);
			  }
		  }
		}
		return null;
	}
}
