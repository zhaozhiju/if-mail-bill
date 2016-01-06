package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;

/**
 * "邮件非账单表"
 */
public interface NotBillCycleInfoDao extends BaseDao<NotBillCycleInfoEntity, Long> {

	/**
	 * 根据用户的地址还有之前储存的原始账单地址查询此用户是否存在
	 * 
	 * @param oldHtml	原始账单地址
	 * @param email		用户地址
	 * @return			数量
	 */
	public int existOfEmail(String oldHtml, String email);
	
	/**
	 * 根据卡号末四位与账单地址查询数据
	 * @param newhtml	新账单地址
	 * @param card		卡号末四位
	 * @return			NotBillCycleInfoEntity
	 */
	public NotBillCycleInfoEntity getDFSURlName(String newhtml, String card);
	
	/**
	 * 根据账单地址查询数据 是否存在
	 * @param newhtml	新账单地址
	 * @return			List<NotBillCycleInfoEntity> 
	 */
	public List<NotBillCycleInfoEntity> getDFSURlByNew(String newhtml);

	/**
	 * 获取用户的账单信息
	 * @param emailUrl
	 * @return List<NotBillCycleInfoEntity> 
	 */
	public List<NotBillCycleInfoEntity> getInfo(String emailUrl);

	/**
	 * 根据这些条件来判断新的数据是否已入库
	 * @param userEmail
	 * @param month
	 * @param bankId
	 * @param billDate
	 * @param billType
	 * @param cardEndOfFour
	 * @return List<NotBillCycleInfoEntity> 
	 */
	public List<NotBillCycleInfoEntity> findMonthExit(String userEmail, String month, int bankId, int billDate,
			int billType, String cardEndOfFour);

	/**
	 * 查询是否有新数据记录进来
	 * @param name
	 * @param decDay
	 * @param bankId
	 * @param billType
	 * @param cardEndOfFour
	 * @return List<NotBillCycleInfoEntity> 
	 */
	public List<NotBillCycleInfoEntity> findDayExit(String name, String decDay,
			int bankId, int billType, String cardEndOfFour);

	/**
	 * 查询此邮箱已存在的数据总数
	 * @param string
	 * @return List<NotBillCycleInfoEntity> 
	 */
	public List<String> findExistEmailCount(String string);

	/**
	 * 通过EmailUrl查询数据
	 * @param emailUrl
	 * @return List<NotBillCycleInfoEntity>
	 */
	public List<NotBillCycleInfoEntity> findByEmailUrl(String emailUrl);

	/**
	 * 根据billCyclePkId查询实体信息
	 * 
	 * @param billCyclePkId
	 * @return NotBillCycleInfoEntity
	 */
	public NotBillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId);

	/**
	 * 批量入库
	 * @param emailUrl
	 */
	public void batchExecute(List<NotBillCycleInfoEntity> emailUrl);

}
