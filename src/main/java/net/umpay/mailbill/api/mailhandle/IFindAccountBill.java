package net.umpay.mailbill.api.mailhandle;

import java.util.List;

import net.umpay.mailbill.hql.model.BillCycleInfoEntity;

/**
 * 查询某用户下绑定邮箱下的 {所有账单、是账单、非账单} 列表接口
 * @author admin
 *
 */
public interface IFindAccountBill {

	/**
	 * 查询某用户下绑定邮箱下的 {所有账单、是账单、非账单} 列表接口
	 * @param account	用户标示
	 * @param billType	账单类型
	 * @return List<BillCycleInfoEntity> 
	 */
	public List<BillCycleInfoEntity> getAccountBill(Long account, int billType);
}
