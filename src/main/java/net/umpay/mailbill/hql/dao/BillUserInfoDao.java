package net.umpay.mailbill.hql.dao;

import java.util.List;

import net.umpay.mailbill.hql.model.BillUserInfoEntity;
import net.umpay.mailbill.util.exception.MailBillException;
/**
 * 邮件账单用户信息(接口)
 */
public interface BillUserInfoDao extends BaseDao<BillUserInfoEntity, Long> {
	
	/**
	 * 根据用户标示去查询用户的信息
	 * @param accountId		用户标示
	 * @return				List<BillUserInfoEntity>
	 */
	public List<BillUserInfoEntity> findMailByAccount(Long accountId) ;
	
	/**
	 * 根据邮箱地址去查询用户的信息
	 * @param mailUrl		邮箱地址
	 * @return				List<BillUserInfoEntity>
	 */
	public List<BillUserInfoEntity> findByMailUrl(String mailUrl) ;
	
	/**
	 * 根据accountId查出该账号处于绑定状态的所有邮箱
	 * 
	 * @param	accountId	用户账号
	 * @return	List<String>
	 */
	public List<String> findMailUrlListByAccountId(Long accountId);
	
	/**
	 * 根据mailUrl查出password
	 * 
	 * @param	mailUrl	用户账号
	 * @return	String
	 */
	public String findPasswordByMailUrl(String mailUrl);
	
	/**
	 * 更改邮箱的绑定状态为未绑定
	 * 
	 * @param	accountId
	 * @param	mailUrl
	 * @return	修改记录条数
	 */
	public int updateToUnbundling(Long accountId, String mailUrl);
	
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
}
