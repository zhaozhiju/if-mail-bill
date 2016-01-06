package net.umpay.mailbill.service.impl.job;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.umpay.mailbill.api.job.IFindBillDateSame;
import net.umpay.mailbill.api.job.IGraspingNewMail;
import net.umpay.mailbill.api.model.job.BillJobView;
import net.umpay.mailbill.api.resolve.IBillJobTempService;
import net.umpay.mailbill.api.resolve.IBillLogService;
import net.umpay.mailbill.api.resolve.IBillPushService;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.BillJobTempEntity;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.hql.model.BillPushEntity;
import net.umpay.mailbill.service.SearchMail;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.util.constants.Constants;
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
 * 遍历任务表中账单日与今天日期相同的邮箱，批量去用户邮箱中抓取邮件账单
 * 若是未抓到邮件的记入临时任务表
 * 账单分为：月账单、日账单
 * 
 * @version 1.0.0
 */
@Service
public class GraspingNewMailImpl implements IGraspingNewMail{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(GraspingNewMailImpl.class);
	
	@Autowired
	private IBillJobTempService billJobTempService;
	@Autowired
	private IFindBillDateSame billDateSame;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private SearchMail searchMail;
	@Autowired
	private IBillLogService billLogService;
	@Autowired
	private IBillPushService billPushService;
	
	@SuppressWarnings({ "static-access" })
	@Override
	public void saveBillJobTemp() {
		// 根据数据去抓取邮件
		String mailUrl = null;
		String password = null;
		List<BillJobView> findDateSame = billDateSame.findDateSame();
		// 日志信息存放
		Map<String, BillLogEntity> billLogInfo = new HashMap<String, BillLogEntity>();
		BillLogEntity entity = new BillLogEntity();
		String acc_phone = null;
		// 获取不重复邮箱地址
		Map<String, String> mail_map_value = getUserEmail(findDateSame);

		if (CollectionUtils.isNotEmpty(findDateSame)) {
			int length = findDateSame.size();
			for (int k = 0; k < length; k++) {
				BillJobView billJobView = findDateSame.get(k);
				mailUrl = billJobView.getUserEmail();
				try {
					if (StringUtils.isNotBlank(mail_map_value.get(mailUrl))) {
						// 对日志信息进行解析封装
						acc_phone = findBillLog(mailUrl, billLogInfo, entity);

						password = billJobView.getPassword();
						if (StringUtils.isNotBlank(mailUrl) && StringUtils.isNotBlank(password)) {
							password = new DesUtil().decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
							searchMail.httpOrJavamail(mailUrl, password, "", acc_phone, billLogInfo);
						}
						findMonthMessage(mailUrl, billJobView);
						mail_map_value.remove(mailUrl);
					} else {
						findMonthMessage(mailUrl, billJobView);
					}
				} catch (MailBillException e) {
					setBillLog(billLogInfo, acc_phone);
					MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
				}
				setBillLog(billLogInfo, acc_phone);
			}
		}
	}
	
	/**
	 * 对日志信息的查询，对新的日志进行封装
	 * 
	 * @param mailUrl
	 * @param billLogInfoMap
	 * @param billLogEntity
	 */
	public String findBillLog(String mailUrl,
			Map<String, BillLogEntity> billLogInfoMap,
			BillLogEntity billLogEntity) {
		// 查询此邮箱最后一次登陆时的accountId与phoneId
		BillLogEntity billLogInfo = billLogService.findEntityByEmail(mailUrl);
		long accountId = billLogInfo.getAccountId();
		String phoneId = billLogInfo.getPhoneId();
		// 给日志实体赋值
		billLogEntity.setAccountId(accountId);
		billLogEntity.setPhoneId(phoneId);
		billLogEntity.setEmailUrl(mailUrl);
		// 封装进map1
		String acc_phone = accountId + "_" + phoneId;
		billLogInfoMap.put(acc_phone, billLogEntity);
		return acc_phone;
	}
	
	/**
	 * 查询是否有需要的信息入库
	 * 
	 * @param mailUrl
	 * @param billJobView
	 */
	private void findMonthMessage(String mailUrl, BillJobView billJobView) throws MailBillException{
		// 查询周期表是否有当前账期数据
		List<BillCycleInfoEntity> findExit = billCycleInfoServiceImpl.findMonthExit(mailUrl,
				new Date(), billJobView.getBankId(), billJobView.getBillDate(),
				billJobView.getBillType(), billJobView.getCardEndOfFour());
		if (findExit.size() == 0) {
			boolean find = billJobTempService.find(
					billJobView.getCardEndOfFour(), billJobView.getUserEmail(),
					billJobView.getBillType());
			if (!find) {
				BillJobTempEntity billJobTempEntity = new BillJobTempEntity();
				billJobTempEntity.setAccountId(billJobView.getAccountId());
				billJobTempEntity.setBankId(billJobView.getBankId());
				billJobTempEntity.setBillDate(billJobView.getBillDate());
				billJobTempEntity.setBillType(billJobView.getBillType());
				billJobTempEntity.setCardEndOfFour(billJobView.getCardEndOfFour());
				billJobTempEntity.setUserEmail(billJobView.getUserEmail());
				billJobTempEntity.setBillStartDate(new Date());
				billJobTempEntity.setIsVaild(1);
				billJobTempService.save(billJobTempEntity);
			} else {
				log.info("信息已存在");
			}
		}else{
			List<BillPushEntity> findByAccountId = billPushService.findByAccountId(billJobView.getAccountId());
			if (findByAccountId.size() == 0){
				BillPushEntity entity = new BillPushEntity();
				entity.setAccountId(billJobView.getAccountId());
				entity.setBillCyclePkIds(findExit.get(0).getId()+",");
				entity.setCreateTime(new Date());
				billPushService.save(entity);
			}else{
				BillPushEntity billPushEntity = findByAccountId.get(0);
				billPushEntity.setBillCyclePkIds(findExit.get(0).getId()+",");
				billPushEntity.setCreateTime(new Date());
				billPushService.save(billPushEntity);
			}
		}
	}
	
	
	@Override
	public void saveDayjob() {
		List<BillJobView> dayBillList = billDateSame.findBillTypeAndDay();
		String mailUrl = null;
		String password = null;
		Map<String, String> mail_map_value = getUserEmail(dayBillList);
		// 日志信息存放
		Map<String, BillLogEntity> billLogInfo = new HashMap<String, BillLogEntity>();
		BillLogEntity entity = new BillLogEntity();
		String acc_phone = null;

		try {
			if (CollectionUtils.isNotEmpty(dayBillList)) {
				int length = dayBillList.size();
				for (int k = 0; k < length; k++) {
					BillJobView billJobView = dayBillList.get(k);
					mailUrl = billJobView.getUserEmail();
					//如果可以从map中取值，说明此邮箱还未进行登录，登陆过后查询信息是否存在；
					//如果取值为空，则表示此邮箱经过登陆了，可以直接去查询是否有自己想要的信息
					if (StringUtils.isNotBlank(mail_map_value.get(mailUrl))) {
						// 对日志信息进行解析封装
						acc_phone = findBillLog(mailUrl, billLogInfo, entity);
						password = billJobView.getPassword();
						if (StringUtils.isNotBlank(mailUrl)&& StringUtils.isNotBlank(password)) {
							password = DesUtil.decrypt3DES(Constants.PASSWORDKEY.getBytes(), password);
							searchMail.httpOrJavamail(mailUrl, password, "", acc_phone, billLogInfo);
						}
						findDayMessage(mailUrl, billJobView);
						//从map中移除掉邮箱地址
						mail_map_value.remove(mailUrl);
					} else {
						findDayMessage(mailUrl, billJobView);
					}
				}
			}
		} catch (MailBillException e) {
			setBillLog(billLogInfo, acc_phone);
			MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		}
		setBillLog(billLogInfo, acc_phone);
	}

	/**
	 * 设置日志信息的公共代码
	 * @param billLogInfo		存放日志信息的map
	 * @param acc_phone			map的key
	 */
	private void setBillLog(Map<String, BillLogEntity> billLogInfo,
			String acc_phone) {
		BillLogEntity billLogEntity = billLogInfo.get(acc_phone);
		if (null != billLogEntity) {
			billLogEntity.setLogoutTime(new Date());
			billLogService.save(billLogEntity);
		}
	}

	/**
	 * 过滤掉重复的邮箱地址，放进map
	 * 
	 * @param billJobList		任务表的信息列表
	 * @return Map<String,String>	不重复的邮箱地址
	 */
	private Map<String, String> getUserEmail(List<BillJobView> billJobList) {
		Set<String> set = new HashSet<String>();
		Map<String, String> map = new HashMap<String, String>();
		int size = billJobList.size();
		for (int i = 0; i < size; i++) {
			set.add(billJobList.get(i).getUserEmail());
		}
		for (String str : set) {
			map.put(str, str);
		}
		return map;
	}

	/**
	 * 查询是否有需要的信息入库
	 * 
	 * @param mailUrl			邮箱地址
	 * @param billJobView		任务实体
	 */
	private void findDayMessage(String mailUrl, BillJobView billJobView) throws MailBillException{
		// 查询周期表是否有今天的日账单
		List<BillCycleInfoEntity> findExit = billCycleInfoServiceImpl.findDayExit(mailUrl,
				new Date(), billJobView.getBankId(), billJobView.getBillType(),
				billJobView.getCardEndOfFour(), 1);// 1:指任务表
		if (findExit.size() == 0) {
			boolean find = billJobTempService.find(
					billJobView.getCardEndOfFour(), billJobView.getUserEmail(),
					billJobView.getBillType());
			if (!find) {
				BillJobTempEntity billJobTempEntity = new BillJobTempEntity();
				billJobTempEntity.setAccountId(billJobView.getAccountId());
				billJobTempEntity.setBankId(billJobView.getBankId());
				billJobTempEntity.setBillType(billJobView.getBillType());
				billJobTempEntity.setCardEndOfFour(billJobView.getCardEndOfFour());
				billJobTempEntity.setUserEmail(billJobView.getUserEmail());
				billJobTempEntity.setBillStartDate(new Date());
				billJobTempEntity.setIsVaild(1);
				billJobTempService.save(billJobTempEntity);
			} else {
				log.info("信息已存在");
			}
		}else{
			List<BillPushEntity> findByAccountId = billPushService.findByAccountId(billJobView.getAccountId());
			if (findByAccountId.size() == 0){
				BillPushEntity entity = new BillPushEntity();
				entity.setAccountId(billJobView.getAccountId());
				entity.setBillCyclePkIds(findExit.get(0).getId()+",");
				entity.setCreateTime(new Date());
				billPushService.save(entity);
			}else{
				BillPushEntity billPushEntity = findByAccountId.get(0);
				billPushEntity.setBillCyclePkIds(findExit.get(0).getId()+",");
				billPushEntity.setCreateTime(new Date());
				billPushService.save(billPushEntity);
			}
		}
	}
	//任务接口
	public void taskJob(){
		saveDayjob();
		saveBillJobTemp();
	}
}
