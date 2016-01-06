package net.umpay.mailbill.api.mailhandle;

import java.util.List;

import net.umpay.mailbill.hql.model.BillUserInfoEntity;

/**
 *  查询某用户下绑定的邮箱列表接口
 *  
 * @author admin
 *
 */
public interface IFindMail {

	/**
	 * 根据用户绑定的用户标示去查询此用户下所有邮箱
	 * 
	 * @param account 	用户的标示
	 * @return			用户所有的邮箱信息
	 */
	public List<BillUserInfoEntity> findMailByAccount(Long account);
}
