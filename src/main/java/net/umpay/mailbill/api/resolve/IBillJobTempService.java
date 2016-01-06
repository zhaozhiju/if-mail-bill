package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.api.model.job.BillJobTempView;
import net.umpay.mailbill.hql.model.BillJobTempEntity;

/**
 * 邮件账单临时任务的接口
 * @author admin
 */
public interface IBillJobTempService {
	/**
	 * 找出临时表中日账单的信息
	 * @return List<BillJobTempView>
	 */
	public List<BillJobTempView> findDayAll();
	/**
	 * 找出临时表中月账单的信息
	 * @return List<BillJobTempView>
	 */
	public List<BillJobTempView> findMonthAll();

	/**
	 * 保存临时表信息
	 * @param billJobTempEntity
	 */
	public void save(BillJobTempEntity billJobTempEntity);

	/**
	 * 删除信息
	 * @param id
	 */
	public void delete(Long id);
	
	/**
	 * 查询信息
	 * @param cardEndOfFour		卡号末四位
	 * @param email				邮箱地址
	 * @param billType			账单类型	
	 * @return  boolean			有返回TRUE，没有返回false
	 */
	public boolean find(String cardEndOfFour, String email, int billType);

	/**
	 * 更新数据的搜索结尾时间
	 * @param entity
	 */
	public void update(BillJobTempView entity);
	
}
