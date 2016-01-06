package net.umpay.mailbill.service.impl.job;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.umpay.mailbill.api.job.IFindNewMail;
import net.umpay.mailbill.api.model.job.BillJobTempView;
import net.umpay.mailbill.api.resolve.IBillJobTempService;
import net.umpay.mailbill.api.resolve.IBillLogService;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.service.SearchMail;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.date.DateCal;
import net.umpay.mailbill.util.date.DateUtil;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.security.DesUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 临时表
 * <p>
 * 	对于月账单：每天要抓取邮箱中账单直至读取到账单（最多持续5个自然日）
 * 	<li>1. 5个自然日内，抓取到账单后，将临时表中该邮箱数据删除</li>
 * 	<li>2. 超出5个自然日，还未抓取到，将临时表该数据设为无效</li>
 * 	对于日账单：每天要抓取邮箱中账单直至读取到账单（最多持续2个自然日）
 * 	<li>同月账单逻辑类似</li>
 * </p>
 * 
 * @version 1.0.0
 */
@Service
public class FindNewMailImpl implements IFindNewMail {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(FindNewMailImpl.class);

	@Autowired
	private IBillJobTempService billJobTempService;
	@Autowired
	private SearchMail searchMail;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private GraspingNewMailImpl graspingNewMailImpl;
	@Autowired
	private IBillLogService billLogService;

	@SuppressWarnings({ "static-access" })
	@Override
	public void findMonthNewMail() {
		// 临时表的月账单全部数据
		List<BillJobTempView> billJobTempList = billJobTempService.findMonthAll();
		String mailUrl = null;
		String password = null;
		// 获取不重复的邮箱地址
		Map<String, String> userEmailMap = getUserEmailMap(billJobTempList);
		// 记录日志信息
		Map<String, BillLogEntity> billLogInfo = new HashMap<String, BillLogEntity>();
		BillLogEntity entity = new BillLogEntity();
		String acc_phone = null;
		if (CollectionUtils.isNotEmpty(billJobTempList)) {
			int length = billJobTempList.size();
			for (int k = 0; k < length; k++) {
				BillJobTempView billJobTempView = billJobTempList.get(k);
				try {
					mailUrl = billJobTempView.getUserEmail();
					if (StringUtils.isNotBlank(userEmailMap.get(mailUrl))) {
						//日志信息的获取填充
						acc_phone = graspingNewMailImpl.findBillLog(mailUrl, billLogInfo, entity);
						password = billJobTempView.getPassword();
						if (StringUtils.isNotBlank(mailUrl) && StringUtils.isNotBlank(password)) {
							password = new DesUtil().decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
							searchMail.httpOrJavamail(mailUrl, password, "", acc_phone, billLogInfo);
						} else {
							log.info("用户名或密码为空！");
						}
						findTempMonth(mailUrl, billJobTempView);
						userEmailMap.remove(mailUrl);
					} else {
						findTempMonth(mailUrl, billJobTempView);
					}
				} catch (MailBillException e) {
					this.setBillLog(billLogInfo, acc_phone);
					MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
				} catch (ParseException e) {
					this.setBillLog(billLogInfo, acc_phone);
					MailBillExceptionUtil.getWithLog(e,
							ErrorCodeContants.DATA_TYPE_CONVERSION_CODE,
							ErrorCodeContants.DATA_TYPE_CONVERSION.getMsg(),
							log);
				}
				this.setBillLog(billLogInfo, acc_phone);
			}
		}
	}

	/**
	 * 查询是否获取新数据，如果没有获取则更新临时表的时间，逾期没有新数据则设为无效
	 * 
	 * @param mailUrl
	 * @param billJobTempView
	 * @return
	 * @throws ParseException
	 */
	private void findTempMonth(String mailUrl, BillJobTempView billJobTempView)
			throws ParseException, MailBillException {
		List<BillCycleInfoEntity> findExit = billCycleInfoServiceImpl.findMonthExit(mailUrl,
				new Date(), billJobTempView.getBankId(),
				billJobTempView.getBillDate(), billJobTempView.getBillType(),
				billJobTempView.getCardEndOfFour());
		if (findExit.size() != 0) {
			billJobTempService.delete(billJobTempView.getId());
		} else {
			Date billStartDate = billJobTempView.getBillStartDate();
			Date billEndDate = billJobTempView.getBillEndDate();
			if (null != billEndDate) {
				String interval = DateCal.interval(DateUtil.getFormatDate(billStartDate), DateUtil.getFormatDate(billEndDate));
				Integer valueOf = Integer.valueOf(interval);
				if (null != valueOf && valueOf > Integer.valueOf(Constants.JOB_DAY)) {
					billJobTempView.setIsVaild(2);
					billJobTempService.update(billJobTempView);
				} else {
					billJobTempService.update(billJobTempView);
				}
			} else {
				billJobTempService.update(billJobTempView);
			}
		}
	}

	@SuppressWarnings({ "static-access" })
	@Override
	public void findDayNewMail() {
		// 临时表的日账单全部数据
		List<BillJobTempView> billJobTemp2DayList = billJobTempService.findDayAll();
		String name = null;
		String password = null;
		Map<String, String> userEmailMap = getUserEmailMap(billJobTemp2DayList);
		// 日志信息的记录
		Map<String, BillLogEntity> billLogInfo = new HashMap<String, BillLogEntity>();
		BillLogEntity entity = new BillLogEntity();
		String acc_phone = null;
		if (CollectionUtils.isNotEmpty(billJobTemp2DayList)) {
			int length = billJobTemp2DayList.size();
			for (int k = 0; k < length; k++) {
				BillJobTempView billJobTempView = billJobTemp2DayList.get(k);
				try {
					name = billJobTempView.getUserEmail();
					if (StringUtils.isNotBlank(userEmailMap.get(name))) {
						// 获取map的key
						acc_phone = graspingNewMailImpl.findBillLog(name, billLogInfo, entity);
						password = billJobTempView.getPassword();
						if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(password)) {
							password = new DesUtil().decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
							searchMail.httpOrJavamail(name, password, "", acc_phone, billLogInfo);
							billJobTempService.delete(billJobTempView.getId());
							userEmailMap.remove(name);
						} else {
							log.info("用户名或密码为空！");
						}
					} else {
						billJobTempService.delete(billJobTempView.getId());
					}
				} catch (MailBillException e) {
					this.setBillLog(billLogInfo, acc_phone);
					MailBillExceptionUtil.getWithLog(e, e.getErrorCode(),
							e.getMessage(), log);
				}
				this.setBillLog(billLogInfo, acc_phone);
			}
		}

	}

	/**
	 * 获取不重复的邮箱地址，放进map
	 * 
	 * @param jobTempViews
	 * @return Map<String, String>
	 */
	private Map<String, String> getUserEmailMap(
			List<BillJobTempView> jobTempViews) {
		Set<String> set = new HashSet<String>();
		Map<String, String> map = new HashMap<String, String>();
		int size = jobTempViews.size();
		for (int i = 0; i < size; i++) {
			set.add(jobTempViews.get(i).getUserEmail());
		}
		for (String str : set) {
			map.put(str, str);
		}
		return map;
	}

	// 抽取公用代码块
	private void setBillLog(Map<String, BillLogEntity> billLogInfo, String acc_phone) {
		BillLogEntity billLogEntity = billLogInfo.get(acc_phone);
		if (null != billLogEntity) {
			billLogEntity.setLogoutTime(new Date());
			billLogService.save(billLogEntity);
		}
	}
}
