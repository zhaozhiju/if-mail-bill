package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillCycleInfoEntity;

/**
 * "邮件账单周期表"
 * @author admin
 *
 */
public interface BillCycleInfoDao extends BaseDao<BillCycleInfoEntity, Long> {

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
	 * @return			BillCycleInfoEntity
	 */
	public BillCycleInfoEntity getDFSURlName(String newhtml, String card);
	
	/**
	 * 根据账单地址查询数据 是否存在
	 * @param newhtml	新账单地址
	 * @return			List<BillCycleInfoEntity> 
	 */
	public List<BillCycleInfoEntity> getDFSURlByNew(String newhtml);

	/**
	 * 获取用户的账单信息
	 * @param emailUrl
	 * @return List<BillCycleInfoEntity> 
	 */
	public List<BillCycleInfoEntity> getInfo(String emailUrl);

	/**
	 * 根据这些条件来判断新的数据是否已入库
	 * @param userEmail
	 * @param month
	 * @param bankId
	 * @param billDate
	 * @param billType
	 * @param cardEndOfFour
	 * @return List<BillCycleInfoEntity> 
	 */
	public List<BillCycleInfoEntity> findMonthExit(String userEmail, String month, int bankId, int billDate,
			int billType, String cardEndOfFour);

	/**
	 * 查询是否有新数据记录进来
	 * @param name
	 * @param decDay
	 * @param bankId
	 * @param billType
	 * @param cardEndOfFour
	 * @return List<BillCycleInfoEntity> 
	 */
	public List<BillCycleInfoEntity> findDayExit(String name, String decDay,
			int bankId, int billType, String cardEndOfFour);

	/**
	 * 查询此邮箱已存在的数据总数
	 * @param string
	 * @return List<BillCycleInfoEntity> 
	 */
	public List<String> findExistEmailCount(String string);

	/**
	 * 通过EmailUrl查询数据
	 * @param emailUrl
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findMonthByEmailUrl(String emailUrl);
	/**
	 * 通过EmailUrl查询数据
	 * @param emailUrl
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findDayByEmailUrl(String emailUrl);

	/**
	 * 根据billCyclePkId查询实体信息
	 * @param billCyclePkId
	 * @return BillCycleInfoEntity
	 */
	public BillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId);
	
	/**
	 * 根据bankId+infoSource+cardEndOfFour在账期表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param infoSource			邮箱地址
	 * @param cardEndOfFour			主卡卡号
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findInfoByInfoSource(int bankId, String infoSource,String cardEndOfFour);
	
	/**
	 * 根据bankId+cardEndOfFour+accountId在账期表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param accountId				用户账号
	 * @param cardEndOfFour			主卡卡号
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findInfoByAccountId(int bankId, Long accountId,String cardEndOfFour);

	/**
	 * 查询当前scVersion序列值
	 * 
	 * @return Long
	 */
	public Long findscVersionSEQ();
	
	/**
	 * 查询卡数据
	 * 
	 * @param mailUrl	用户email地址
	 * @param accountId	用户账号
	 * @param csVersion	服务端数据版本号
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findCardsInfo(String mailUrl, Long accountId, String csVersion);
	
	/**
	 * 查询卡下月账单账期数据
	 * 
	 * @param mailUrl		用户email地址
	 * @param accountId		用户账号
	 * @param csVersion		客户端版本号
	 * @param bankId		银行ID
	 * @param cardEndOfFour	卡号末四位
	 * @param billType		账单类型(1 月账单;  2 日账单;)
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findMonthtCylesInfo(String mailUrl, Long accountId,
			String csVersion, long bankId, String cardEndOfFour, Integer billType);
	
	/**
	 * 查询卡下月账单账期数据(无卡号)
	 * 
	 * @param mailUrl		用户email地址
	 * @param accountId		用户账号
	 * @param csVersion		客户端版本号
	 * @param userName		用户姓名
	 * @param paymentDate	还款日期(dd)
	 * @param bankId		银行ID
	 * @param billType		账单类型(1 月账单;  2 日账单;)
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findMonthtCylesInfoNoCardNum(String mailUrl, Long accountId,
			String csVersion, String userName, String paymentDate, long bankId, Integer billType);
}
