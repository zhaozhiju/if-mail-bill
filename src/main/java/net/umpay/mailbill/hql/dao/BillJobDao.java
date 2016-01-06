package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillJobEntity;
/**
 * 邮件账单定时任务接口
 * @author admin
 *
 */
public interface BillJobDao extends BaseDao<BillJobEntity, Long> {

	/**
	 * 查询跟当前日期相同的信息
	 * @param day 	当前日期
	 * @return      List<BillJobEntity> 
	 */
	public List<BillJobEntity> findBillDateAndDay(int day);
	/**
	 * 查询日账单信息
	 * @return	List<BillJobEntity> 
	 */
	public List<BillJobEntity> findBillTypeAndDay();

	/**
	 * 查询这些信息是否存在
	 * 
	 * @param bankId		
	 * @param billDate
	 * @param billType
	 * @param cardEndOfFour
	 * @param userEmail
	 * @return boolean
	 */
	public boolean findJob(int bankId, int billDate, int billType,
			String cardEndOfFour, String userEmail);
	/**
	 * 查询这些信息是否存在
	 * 
	 * @param bankId		
	 * @param billType
	 * @param cardEndOfFour
	 * @param userEmail
	 * @return boolean
	 */
	public boolean findDayJob(int bankId, int billType,
			String cardEndOfFour, String userEmail);
	
}
