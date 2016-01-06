package net.umpay.mailbill.util.mail.httpmail;

import java.util.List;
import java.util.Map;

import net.umpay.mailbill.api.httpmail.IHttpMail;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.mail.MailAnalyze;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * http方式工厂类
 * @version 1.0.0
 */
@Service
public class HttpMailFactory {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpMailFactory.class);
	
	@Autowired
	private MailAnalyze mailAnalyze;
	// spring 注入
	private List<IHttpMail> httpScanTypeList;
	
	public List<IHttpMail> getHttpScanTypeList() {
		return httpScanTypeList;
	}

	public void setHttpScanTypeList(List<IHttpMail> httpScanTypeList) {
		this.httpScanTypeList = httpScanTypeList;
	}

	/**
	 * 根据后缀选择登录模板
	 * @param mailUrl				邮箱地址
	 * @param password				密码信息
	 * @param key					websocket长连接标示
	 * @param accountid_phoneId		日志map信息的key值
	 * @param logMap				日志map
	 * @throws MailBillException 
	 */
	
	public void getHttpMailFactory(String mailUrl,String password, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap) throws MailBillException{
		//分解accountId与phoneId
		String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
		
		// TODO 每个邮箱生成对应的自有邮箱，作为转发邮箱
		String forwardMail = "zzj99@126.com";
		String scanType = mailAnalyze.getHttpScanType(mailUrl);

		if (CollectionUtils.isNotEmpty(httpScanTypeList)) {
			for (IHttpMail iHttpMail : httpScanTypeList) {
				if (iHttpMail.httpScanType().equals(scanType)) {
					log.info("e_mail:{} \taccount_id:{} \tphone_id:{} http scan\tforward_mail:{}", new Object[]{mailUrl, accountId, phoneId, forwardMail});
					iHttpMail.httpScan(mailUrl, password, forwardMail, key, accountid_phoneId, logMap);
					break;
				}
			}
		} else {
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.HTTP_SCAN_TYPE_LIST_EXCEPTION_CODE, ErrorCodeContants.HTTP_SCAN_TYPE_LIST_EXCEPTION.getMsg(), log);
		}
	}
}
