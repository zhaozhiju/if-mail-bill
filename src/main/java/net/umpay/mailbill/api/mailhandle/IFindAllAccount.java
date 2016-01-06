package net.umpay.mailbill.api.mailhandle;

import java.util.List;

import net.umpay.mailbill.hql.model.BillUserInfoEntity;

/**
 * 查询所有用户的账号 及密码
 * 
 * @author admin
 *
 */
public interface IFindAllAccount {

	/**
	 * 查询所有用户的账号跟密码
	 * 
	 * @return	账号与密码的集合
	 */
	public List<BillUserInfoEntity> findAccount();
	
}
