package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillJobEntity;

/**
 * 邮件账单定时任务的接口
 * @author admin
 */
public interface IBillJobService {

	/**
	 * 在任务表内添加卡信息
	 * @param billCycleInfoEntity
	 */
	public void saveJob(BillCycleInfoEntity billCycleInfoEntity);
	
	/**
	 * 找出跟当前日期相同的账单日并返回信息
	 * @return List<BillJobEntity>
	 */
	public List<BillJobEntity> findBillDateAndDay();
	
	/**
	 * 找出跟当前日期相同的账单日并返回信息
	 * @return List<BillJobEntity>
	 */
	public List<BillJobEntity> findBillTypeAndDay();
	
	/**
	 * 查询此条信息是否存在
	 * 
	 * @param bankId		银行卡标示
	 * @param billDate		账单日
	 * @param billType		账单类型
	 * @param cardEndOfFour	卡号末四位
	 * @param userEmail		用户地址
	 * @return boolean
	 */
	public boolean findJob(int bankId, int billDate, int billType, String cardEndOfFour, String userEmail);
	/**
	 * 查询此条信息是否存在
	 * 
	 * @param bankId		银行卡标示
	 * @param billType		账单类型
	 * @param cardEndOfFour	卡号末四位
	 * @param userEmail		用户地址
	 * @return boolean
	 */
	public boolean findDayJob(int bankId, int billType, String cardEndOfFour, String userEmail);
}
