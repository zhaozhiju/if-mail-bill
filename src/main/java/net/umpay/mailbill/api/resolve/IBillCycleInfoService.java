package net.umpay.mailbill.api.resolve;

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.util.exception.MailBillException;
/**
 * 邮件账单周期表
 * @author admin
 */
public interface IBillCycleInfoService {
	
	/**
	 * 保存实体
	 * @param entity 实体
	 */
	public void save(BillCycleInfoEntity entity) throws MailBillException;
	
	/**
	 * 根据accountId来查询账单周期表的主键列表
	 * @param emailUrl
	 * @return 主页面VO JspInfoView
	 */
	public List<JspInfoPartView> findEntityByEmailUrl(String emailUrl) throws MailBillException;
	
	/**
	 * 	根据原始账单的地址来查询账期表内的信息
	 * @param oldhtml	邮件的截取后的地址
	 * @return	账期表的实体
	 */
	public List<BillCycleInfoEntity> getDFSURlByNew(String oldhtml) throws MailBillException;
	
	/**
	 * 查询是否存在数据
	 * @param oldhtml	原始存放目录
	 * @param email		用户邮箱地址
	 * @return 			数据总数
	 */
	public int existOfEmail(String oldhtml, String email) throws MailBillException;
	
	/**
	 * 获取DFS的地址信息
	 * @param newhtml	截取后存放目录
	 * @param card		卡号末四位
	 * @return			BillCycleInfoEntity
	 */
	public BillCycleInfoEntity getDFSURlName(String newhtml, String card)throws MailBillException;
	/**
	 * 获取账户Email的账单信息
	 * @param emailUrl	用户地址
	 * @return			List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> getInfo(String emailUrl)throws MailBillException;
	
	/**
	 * 查询数据是否已经插入
	 * @param userEmail
	 * @param month
	 * @param bankId
	 * @param billDate
	 * @param billType
	 * @param cardEndOfFour
	 * @return	List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findMonthExit(String userEmail, Date month, int bankId, int billDate,
			int billType, String cardEndOfFour) throws MailBillException;
	
	/**
	 * 根据billCyclePkId查询实体信息
	 * @param billCyclePkId
	 * @return	BillCycleInfoEntity
	 */
	public BillCycleInfoEntity findEntityByBillCyclePkId(Long billCyclePkId) throws MailBillException;
	
	/**
	 * 根据信息查询是否存在此条信息
	 * @param name
	 * @param date
	 * @param bankId
	 * @param billType
	 * @param cardEndOfFour
	 * @param flag
	 * @return	List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findDayExit(String name, Date date, int bankId, int billType, String cardEndOfFour , int flag) throws MailBillException;
	
	/**
	 * 查询数据库已存在的数据，并保存到map中
	 * 
	 * @param string
	 * @return Map<String, String> 
	 */
	public Map<String, String> findExistEmailCount(String string) throws MailBillException;
	
	/**
	 * 根据银行id+邮箱地址+主卡卡号在账期表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param infoSource			邮箱地址
	 * @param cardEndOfFour			主卡卡号
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findInfoByBIC(int bankId, String infoSource,String cardEndOfFour) throws MailBillException;
	
	/**
	 * 根据银行id+用户账号+主卡卡号在账期表中查询信息
	 * 
	 * @param bankId				银行id
	 * @param accountId				用户账号
	 * @param cardEndOfFour			主卡卡号
	 * @return List<BillCycleInfoEntity>
	 */
	public List<BillCycleInfoEntity> findInfoByBAC(int bankId, Long accountId, String cardEndOfFour) throws MailBillException;
	
	/**
	 * 返回一个scVersion序列值
	 * 
	 * @return long
	 */
	public Long findscVersionSEQ() throws MailBillException;;
}
