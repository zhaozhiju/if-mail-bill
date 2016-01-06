package net.umpay.mailbill.api.job;

/**
 * 遍历任务表中账单日与今天日期相同的邮箱，批量去用户邮箱中抓取邮件账单
 * 若是未抓到邮件的记入临时任务表
 * 账单分为：月账单、日账单
 * @version 1.0.0
 */
public interface IGraspingNewMail {

	/**
	 * 月账单定时任务
	 * 遍历任务表中属于月账单类型，账单日与今天日期相同的邮箱，批量去用户邮箱中抓取邮件账单
	 * 若未抓到账，则将邮件的信息记录到任务临时表
	 */
	public void saveBillJobTemp();
	
	/**
	 * 日账单定时任务
	 * 遍历任务表中属于日账单类型的邮箱，每天批量去用户邮箱中抓取邮件账单
	 * 若未抓到账，则将邮件的信息记录到任务临时表
	 * 将为抓取到邮件的日账单记录到临时表内
	 */
	public void saveDayjob();
}
