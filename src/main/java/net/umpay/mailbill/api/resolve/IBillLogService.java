package net.umpay.mailbill.api.resolve;

import net.umpay.mailbill.hql.model.BillLogEntity;

/**
 * 记录操作日志详细信息
 * @author admin
 */
public interface IBillLogService {
	
	/**
	 * 开始保存日志信息值	
	 */
	public void save(BillLogEntity entity);
	/**
	 * 根据id来获取实体对象
	 * @param id
	 * @return BillLogEntity
	 */
	public BillLogEntity findEntityById(long id);
	
	/**
	 * 根据Email地址来获取实体信息
	 * @param mail		邮箱地址
	 * @return			BillLogEntity
	 */
	public BillLogEntity findEntityByEmail(String mail);
	
	/**
	 * 先new出一个实体来存放日志信息
	 * @param phoneId		手机id
	 * @param accountId		用户id
	 * @param emailUrl		登陆地址
	 * @return	BillLogEntity
	 */
	public BillLogEntity getBillLog(String phoneId, Long accountId, String emailUrl);
}
