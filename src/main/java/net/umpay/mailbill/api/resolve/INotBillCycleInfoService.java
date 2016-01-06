package net.umpay.mailbill.api.resolve;

import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;
import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;
import net.umpay.mailbill.util.exception.MailBillException;

/**
 * 邮件非账单周期表
 */
public interface INotBillCycleInfoService {

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 */
	public void save(NotBillCycleInfoEntity entity) throws MailBillException;

	/**
	 * 根据accountId来查询账单周期表的主键列表
	 * 
	 * @param emailUrl
	 * @return 主页面VO JspInfoView
	 */
	public List<JspInfoPartView> findEntityByEmailUrl(String emailUrl)
			throws MailBillException;

	/**
	 * 获取DFS的地址信息
	 * 
	 * @param newhtml
	 *            截取后存放目录
	 * @param card
	 *            卡号末四位
	 * @return NotBillCycleInfoEntity
	 */
	public NotBillCycleInfoEntity getDFSURlName(String newhtml, String card)
			throws MailBillException;

	/**
	 * 获取账户Email的账单信息
	 * 
	 * @param emailUrl
	 *            用户地址
	 * @return List<NotBillCycleInfoEntity>
	 */
	public List<NotBillCycleInfoEntity> getInfo(String emailUrl)
			throws MailBillException;

	/**
	 * 查询数据库已存在的数据，并保存到map中
	 * 
	 * @param string
	 * @return Map<String, String>
	 */
	public Map<String, String> findExistEmailCount(String string)
			throws MailBillException;

	/**
	 * 查询数据是否存在
	 * 
	 * @param oldhtml
	 *            拼接地址
	 * @param email
	 *            邮箱地址
	 * @return int
	 */
	public int existOfEmail(String oldhtml, String email)
			throws MailBillException;

	/**
	 * 根据新地址判断查询DFS地址
	 * 
	 * @param newhtml
	 * @return List<NotBillCycleInfoEntity>
	 */
	public List<NotBillCycleInfoEntity> getDFSURlByNew(String newhtml)
			throws MailBillException;
}
