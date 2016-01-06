package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.util.exception.MailBillException;

/**
 * 邮件账单用户信息
 */
public interface IBillUserInfoService {
	/**
	 * 保存邮件地址与用户账号的绑定关系
	 * <p>
	 * 	<li>同一账号最多绑定邮箱的数量上限为固定值</li>
	 * </p>
	 * 
	 * @param emailUrl	邮件地址
	 * @param password	密码
	 * @param accountId 用户账号
	 * @throws MailBillException 
	 */
	public void save(String emailUrl, String password, Long accountId) throws MailBillException;
	/**
	 *  查询邮箱用户是否存在
	 * @param emailUrl	邮箱地址
	 * @return  BillUserInfoEntity
	 */
	public BillUserInfoEntity findUser(String emailUrl);
	
	/**
	 *  查询邮箱用户是否已绑定
	 *  
	 * @param emailUrl	邮箱地址
	 * @param accountId 用户账号
	 * @return  boolean	true 已绑定; false 未绑定;
	 * add by zhaozj on 2014/10/20
	 */
	public boolean userIfBindingMail(String emailUrl, Long accountId) throws MailBillException;
	
	/**
	 * 查询所有用户的账号与密码
	 * @return	List<BillUserInfoEntity>
	 * @throws MailBillException
	 */
	public List<BillUserInfoEntity> findAll() throws MailBillException;
	/**
	 * 
	 * 根据用户标示来查询
	 * 	用户的账号与密码
	 * @return List<BillUserInfoEntity>
	 */
	public List<BillUserInfoEntity> findMailByAccount(Long account) throws MailBillException;
	
	/**
	 * 将用户的绑定状态设为已绑定并更新密码
	 * 
	 * @param	accountId
	 * @param	mailUrl
	 * @param   password
	 * @return 	更改记录条数
	 * @throws MailBillException 
	 */
	public int updateToBinding(Long accountId, String mailUrl, String password) throws MailBillException;
	
	
	/**
	 * 根据邮箱地址查询数据
	 * 
	 * @param	mailUrl
	 * @return	List<BillUserInfoEntity>
	 */
	public List<BillUserInfoEntity> findByMailUrl(String mailUrl);
	
	/**
	 * 根据accountId查询该用户绑定的全部邮箱列表
	 * 
	 * @param accountId
	 */
	public List<String> findMailUrlList(Long accountId);
	
	/**
	 * 根据邮箱地址查出该邮箱的密码
	 * 
	 * @param	mailUrl	邮箱地址
	 * @return	String	密码
	 */
	public String findPasswordByMailUrl(String mailUrl);
	
	/**
	 * 将指定用户的指定邮箱解绑
	 * 
	 * @param	accountId	用户账号
	 * @param	mailUrl		邮箱地址
	 * @return 	int			修改条数
	 */
	public int updateToUnbundling(Long accountId, String mailUrl);
}
