package net.umpay.mailbill.api.pushapp;

import net.umpay.mailbill.api.model.pushapp.Card;
import net.umpay.mailbill.api.model.pushapp.CardDetial;
import net.umpay.mailbill.api.model.pushapp.Cycle;
import net.umpay.mailbill.api.model.pushapp.DayBill;
import net.umpay.mailbill.api.model.pushapp.MonthBill;
import net.umpay.mailbill.util.exception.MailBillException;

/**
 * <pre>
 *   <li>以webSocket长链接多次向客户端 发送数据形式分别推送:</li>
 *   <li>1.卡数据;2.账期数据;3.详情数据;</li>
 *   <li>目的：支持客户端的进度平滑显示</li>
 * </pre>
 * 
 * @author zhaozj add on 2014/10/28
 */
public interface IPushMailBill2App {

	/**
	 * 推送用户账号解绑邮箱
	 * 
	 * @param emailUrl
	 *            邮箱地址
	 * @param accountId
	 *            用户账号
	 * @param phoneId
	 *            手机标示
	 * @param socketKey
	 *            socket长连接唯一标示
	 * @throws MailBillException
	 */
	public void pushUnBindingMail(String emailUrl, Long accountId,
			String phoneId, String socketKey) throws MailBillException;

	/**
	 * 推送用户账号下有效的邮箱列表
	 * 
	 * @param emailUrl
	 *            邮箱地址
	 * @param accountId
	 *            用户账号
	 * @param phoneId
	 *            手机标示
	 * @param socketKey
	 *            socket长连接唯一标示
	 * @throws MailBillException
	 */
	public void pushAccountOfMails(String emailUrl, Long accountId,
			String phoneId, String socketKey) throws MailBillException;

	/**
	 * 推送数据入口
	 * 
	 * @param mailUrl
	 *            邮箱url
	 * @param accountId
	 *            用户账号
	 * @param phoneId
	 *            手机标示
	 * @param csVersion
	 *            客户端数据版本号
	 * @param socketKey
	 *            socket长连接的唯一标示
	 * @throws MailBillException
	 */
	public void pushMailBillEntrance(String mailUrl, Long accountId,
			String phoneId, String csVersion, String socketKey)
			throws MailBillException;

	/**
	 * 获取卡数据
	 * 
	 * <p>
	 * <li>卡分为月账单和日账单两种类型</li>
	 * <li>1. 月账单:同一银行下,同一卡号只返回一个(key:bankId+cardNo+月账单标示),对无卡号用还款日+名称加以区分(key:bankId+到期还款日+用户名称+月账单标示);</li>
	 * <li>2. 日账单:同一银行,同一卡号只返回一个(key:bankId+cardNo+日账单标示);</li>
	 * </p>
	 * 
	 * @param mailUrl
	 *            邮箱url
	 * @param accountId
	 *            用户账号
	 * @param csVersion
	 *            客户端数据版本号
	 * @return Card 卡数据
	 * @throws MailBillException
	 */
	public Card getCardsInfo(String mailUrl, Long accountId, String csVersion)
			throws MailBillException;

	/**
	 * 获取卡下的月账单账期数据
	 * 
	 * @param mailUrl
	 *            邮箱url
	 * @param accountId
	 *            用户账号
	 * @param csVersion
	 *            客户端数据版本号
	 * @param userName
	 *            卡所属人名字
	 * @param bankId
	 *            银行id
	 * @param cardEndOfFour
	 *            卡号末四位
	 * @param paymentDate
	 * 		                  还款日
	 * @param cardDetial
	 * 			     卡对象
	 * @return	Cycle
	 * @throws MailBillException
	 */
	public Cycle geMonthtCylesInfo(String mailUrl, Long accountId,
			String csVersion, String userName, long bankId, String cardEndOfFour, String paymentDate, CardDetial cardDetial)
			throws MailBillException;

	/**
	 * 获取卡下的日账单账期数据
	 * 
	 * @param mailUrl
	 *            邮箱url
	 * @param accountId
	 *            用户账号
	 * @param csVersion
	 *            客户端数据版本号
	 * @param userName
	 *            卡所属人名字
	 * @param bankId
	 *            银行id
	 * @param cardEndOfFour
	 *            卡号末四位
	 * @param cardDetial
	 * 			    卡对象
	 * @return Cycle
	 * @throws MailBillException
	 */
	public Cycle getDayCylesInfo(String mailUrl, Long accountId,
			String csVersion, String userName, long bankId, String cardEndOfFour, CardDetial cardDetial)
			throws MailBillException;

	/**
	 * 获取月账单的详情
	 * 
	 * @param cyclePkId
	 *            账单周期表主键
	 * @param cardDetial
	 * 			  卡对象
	 * @return MonthBill
	 * @throws MailBillException
	 */
	public MonthBill getMonthBillsInfo(long cyclePkId, CardDetial cardDetial) throws MailBillException;

	/**
	 * 获取日账单详情
	 * 
	 * @param cyclePkId
	 *            账单周期表主键
	 * @param cardDetial
	 * 			  卡对象
	 * @return DayBill
	 * @throws MailBillException
	 */
	public DayBill getDayBillsInfo(long cyclePkId, CardDetial cardDetial) throws MailBillException;

}
