package net.umpay.mailbill.api.job;

import java.util.List;

import net.umpay.mailbill.api.model.job.BillJobView;

/**
 *  每天检查所有用户的账单日与当前日期相同（如：21号），
 *  返回所有邮箱地址及密码、user_id ，卡号末四位，账单日的接口
 * @author admin
 *
 */
public interface IFindBillDateSame {

	/**
	 * 如果账单日与当前日期相同的话，
	 * 	返回所有邮箱地址及密码、user_id ，卡号末四位，账单日的接口
	 * @return List<BillJobView>
	 */
	public List<BillJobView> findDateSame();
	/**
	 * 日账单
	 * 	返回所有邮箱地址及密码、user_id ，卡号末四位，账单日的接口
	 * @return List<BillJobView>
	 */
	public List<BillJobView> findBillTypeAndDay();
}
