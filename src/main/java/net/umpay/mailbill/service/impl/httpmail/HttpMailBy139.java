package net.umpay.mailbill.service.impl.httpmail;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import net.umpay.mailbill.api.httpmail.IHttpMail;
import net.umpay.mailbill.entrance.websocket.MyWebSocket;
import net.umpay.mailbill.hql.model.BillLogEntity;
import net.umpay.mailbill.hql.model.ThreadRunAbleTest;
import net.umpay.mailbill.service.impl.banktemplate.ParseMailService;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.mail.MailAnalyze;
import net.umpay.mailbill.util.mail.httpmail.HttpClientHelper;
import net.umpay.mailbill.util.mail.httpmail.HttpResult;
import net.umpay.mailbill.util.security.Base64;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 139邮箱http抓取邮件
 * 
 * @author zhaozj
 * Add on 2014/08/15
 */
@Service
public class HttpMailBy139 extends MyWebSocket implements IHttpMail{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpMailBy139.class);

	@Autowired
	private MailAnalyze mailAnalyze;
	@Autowired
	private ParseMailService parseMailService;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor ;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	
	// 登录相关的链接
	private final String SESSION_INIT = "http://mail.10086.cn/";
	private final String LOGIN_URL = "https://mail.10086.cn/Login/Login.ashx?_fv=4&cguid={0}&_=14d58fe9da760748badb8b63d6f56a81f1ec90aa";
    // 邮箱文件夹列表列表
	private final String  ALL_FOLDERS_URL = "http://appmail.mail.10086.cn/s?func=mbox:getAllFolders&sid={0}&&comefrom=54&cguid={1}";
    // 各文件夹下的邮件列表请求链接
	private final String FOLDER_URL = "http://appmail.mail.10086.cn/s?func=mbox:listMessages&sid={0}&&comefrom=54&cguid={1}";
    // 获取邮件内容
	private final String GET_MAIL_INFO = "http://appmail.mail.10086.cn/RmWeb/view.do?func=view:readMessage&comefrom=54&sid={0}&cguid={1}&mid={2}&callback=readMailReady&fid={3}";
    // 邮箱退出登录链接
	private final String LOGOUT_MAIL_URL = "http://mail.10086.cn/login/Logout.aspx?sid={0}&redirect=http://mail.10086.cn/logout.htm?code=6_600";
    // 设置转发规则相关的链接
	private final String RULE_POST_URl = "http://appmail.mail.10086.cn/s?func=user:setFilter_New&sid={0}&&comefrom=54&cguid={1}";
    // 所有转发规则列表链接
	private final String RULE_LIST_URl = "http://appmail.mail.10086.cn/s?func=user:getFilter_New&sid={0}&&comefrom=54&cguid={1}";
    // 转发关键字
	private final String FORWARD_KEY = Constants.FORWARD_KEY;
	
	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_139_TYPE;
	}
  	
    /**
     * mailUrl 要截掉@139.com
     */
    
	@SuppressWarnings("static-access")
	@Override
	public void httpScan(String mailUrl, String password, String forwardMail, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap)
			throws MailBillException {
    	BillLogEntity billLoggerEntity = logMap.get(accountid_phoneId);

    	//分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
    	
    	// [1]. 登录邮箱
    	HttpClientHelper hc = new HttpClientHelper(true);
        HttpResult lr = hc.get(SESSION_INIT);// 目的是得到 csrfToken 类似
        Map<String, String> paremeters = new HashMap<String, String>();
        StringBuffer sbStr = new StringBuffer();
        String vCode = "";
        lr = mailLogin(hc, lr, mailUrl, password,vCode, paremeters);
        String cguid = paremeters.get("cguid");
        
        //判断是否需要验证码
        boolean needVcode = vcodeJudge(lr,hc,key);
        while(needVcode){
        	this.send("A1`请输入验证码...",key);
        	String code = super.vcodeMap.get(key);
        	while(code == null){
        		code = super.vcodeMap.get(key);
        	}
        	
        	//刷新验证码
	    	String[] infoArray = new String[3];
	    	if(code.contains("refreshVcode")){
	    		infoArray = refreshVcode(lr, hc, infoArray, key);
	    	}else{
	    		String loginInfo = code.substring(code.indexOf("Client:RD1`")+11, code.length());
	    		infoArray = loginInfo.split("\\{umP\\}");
	    	}
        	
        	lr = mailLogin(hc, lr, infoArray[0],infoArray[1],infoArray[2], paremeters);
        	this.vcodeMap.remove(key);
        	needVcode = vcodeJudge(lr,hc,key);
        	if(needVcode){
        		this.send("A1`登录失败，请检查您输入的信息...", key);
        	}
        }
        
        // 获取header信息中两个参数：随机数和登录时间
        String rnd = "";
        String mtime = "";
        String[] parArr = getPar(lr, key, mailUrl);
        if(null != parArr && parArr.length == 2){
			rnd = parArr[0];
			mtime = parArr[1];
		} else {
			log.error("e_mail:{} \taccount_id:{} \tphone_id:{} get parameters rnd and mtime error", new Object[]{mailUrl, accountId, phoneId});
		}

        String sid = hc.getCookie("Os_SSo_Sid", "mail.10086.cn");
        if(null != sid){
        	this.send("A1`登录成功！", key);
        	
        	// 从序列中获取一个long值作为版本号
			Long scVersion = billCycleInfoServiceImpl.findscVersionSEQ();
			
        	if (billLoggerEntity != null){
        		billLoggerEntity.setLogOn(MailBillTypeConstants.SUCCESS);
        	}
        	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} http login success", new Object[]{mailUrl, accountId, phoneId});
        	// [2]. 关闭邮箱登录短信提醒 TODO 目前先不做
        	
        	// [3]. 设置转发规则  二期不需转发功能,暂时先关闭该功能 TODO
        	/*boolean setMailFilters = setMailFilters( hc, lr, sid, cguid, sbStr, forwardMail, mailUrl);
        	 if (billLoggerEntity != null){
             	billLoggerEntity.setForwardResults(setMailFilters ? MailBillTypeConstants.SUCCESS : MailBillTypeConstants.FAILED);
             }*/
        	// [4]. 收集文件夹相关信息
        	Map<String, String[]> folder_count = getAllFlodersInfo(hc, lr, sid, cguid, rnd, mtime, sbStr);
        	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfolder_count:{}", new Object[]{mailUrl, accountId, phoneId, folder_count});
        	// [5]. 获取各文件夹下的匹配关键字的邮件
        	fliterFloderByKey(hc, lr, sid, cguid, rnd, mtime, folder_count,  sbStr, mailUrl, key, accountid_phoneId, logMap, scVersion);
        	// [9]. web-socket相关信息
        	// [10]. 退出邮箱
        	lr = hc.get(MessageFormat.format(LOGOUT_MAIL_URL, sid), set139LogoutMail(sid, cguid));
        	String string = lr.getHtml().length()>0?"退出成功":"退出失败";
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \thttp logout:{}", new Object[]{mailUrl, accountId, phoneId, string});
        } else {
        	if (billLoggerEntity != null){
        		billLoggerEntity.setLogOn(MailBillTypeConstants.FAILED);
        	}
        	this.send("A1`登录失败，请检查您的邮箱用户名与密码...", key);
        	throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE, ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
        }
	}
	
	// -------- private function --------
    //验证码
    private boolean vcodeJudge(HttpResult lr,HttpClientHelper hc,String key){
    	boolean flag = false;
    	List<Header> headers = lr.getHeaders();
    	String location = "";
    	if (null != headers && headers.size() > 0) {
			Object[] headersArr = headers.toArray();
			int header_length = headersArr.length - 1;
			for (int k = header_length; k > 0; k--) {
				if(headersArr[k].toString().contains("Location")){
					location = headersArr[k].toString();
					//v = 1 需要手动输入验证码  v = 0 无需输入验证码
					if(location.contains("&v=1")){
						flag = true;
						vCodeImgUrl(lr, hc, key);
					}
				}
			}
		}
    	return flag;
    }
    
    //登录验证码请求链接
    private void vCodeImgUrl(HttpResult lr, HttpClientHelper hc, String key){
    	lr = hc.get("http://imagecode.mail.10086.cn/getimage?clientid=1&r=0.48710639006458223", set139GetVcodeImg());
    	this.send("D1`验证码url为："+"data:image/jpg;base64,"+Base64.encode(lr.getResponse()),key);
    }
    
    //
    
	@SuppressWarnings("static-access")
	private String[] refreshVcode(HttpResult lr, HttpClientHelper hc, String[] info, String key){
    	while(true){
    		String code = super.vcodeMap.get(key);
        	while(code == null){
        		code = super.vcodeMap.get(key);
        	}
        	if(code.contains("RD1`")){
        		String loginInfo = code.substring(code.indexOf("Client:RD1`")+11, code.length());
        		info = loginInfo.split("\\{umP\\}");
        		break;
        	}
        	if(code.contains("refreshVcode")){
        		vCodeImgUrl(lr, hc, key);
        		this.vcodeMap.remove(key);
        	}
    	}
		return info;
    }
    
    //获取随机数和时间两个参数: parArr[0]:rnd;  parArr[1]:mtime
	protected String[] getPar(HttpResult lr, String key, String mailUrl) throws MailBillException{
		String[] parArr = new String[2];
		List<Header> headers = lr.getHeaders();
		if (null != headers && headers.size() > 0) {
			Object[] headersArr = headers.toArray();
			int header_length = headersArr.length - 1;
			for (int k = header_length; k > 0; k--) {
				String _header = headersArr[k].toString();
				int index = _header.indexOf("Location");
				if (index != -1) {
					parArr = getRnd_Mtime(_header, key, mailUrl);
					break;
				} else {
					continue;
				}
			}
		}
		return parArr;
	}
    
    // 时间处理
	private String conversionDate(String receiveDate,
			SimpleDateFormat formatter) {
		String formatterDate = "";
		if (StringUtils.isNotBlank(receiveDate)) {
			Date date = new Date(Long.parseLong(receiveDate + "000"));
			formatterDate = formatter.format(date);
		} else {
			Date date = new Date(System.currentTimeMillis());
			formatterDate = formatter.format(date);
		}
		return formatterDate;
	}
	
    // 处理收发邮件地址  截取<>
	private String chengMailUrl(String mailUrl) {
		String url = "";
		if (StringUtils.isNotBlank(mailUrl) && mailUrl.indexOf("<") != -1) {
			int begin = mailUrl.indexOf("<");
			int end = mailUrl.indexOf(">");
			url = mailUrl.substring(begin + 1, end);
		} else {
			url = mailUrl;
		}

		return url;
	}
    
    // -------- public function -------

	/**
	 * 邮箱登录
	 * 
	 * @param hc
	 * @param lr
	 * @param mailUrl
	 * @param password
	 * @param vCode
	 * @param paremeters
	 * @return HttpResult
	 */
	public HttpResult mailLogin(HttpClientHelper hc, HttpResult lr, String mailUrl, String password ,String vCode , Map<String, String> paremeters){
        // 截取cguid参数
        String responseUrl = lr.getResponseUrl();
        String cguid = getCguid(responseUrl);
        // [1]. 拼装登录信息
        Map<String, String> data = new HashMap<String, String>();
        if(StringUtils.isNotBlank(mailUrl) && mailUrl.indexOf("@139") != -1){
        	mailUrl = mailUrl.substring(0, mailUrl.indexOf("@139"));
        }
        data.put("VerifyCode", vCode);
        data.put("UserName", mailUrl);
        data.put("Password", password);
        lr = hc.post(MessageFormat.format(LOGIN_URL, cguid), data, set139LoginHeader());// 执行登录
        paremeters.put("cguid", cguid);
        return lr;
	}
	
    /**
     * 根据关键字过滤各文件下的邮件
     * 
     * @param hc
     * @param lr
     * @param sid
     * @param cguid
     * @param rnd
     * @param mtime
     * @param folder_count
     * @param sbStr
     * @param mailUrl
     * @param key
     * @throws MailBillException 
     */
	
	public void fliterFloderByKey(HttpClientHelper hc, HttpResult lr,
			String sid, String cguid, String rnd, String mtime, Map<String, String[]> folder_count,
			StringBuffer sbStr,String mailUrl, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap, Long scVersion) throws MailBillException {
    	// [5]. 获取各文件夹下的匹配关键字的邮件
    	int count = 0; //统计账单邮件数
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm");
    	
    	//查询数据库内已存在的数据
    	Map<String, String> map = billCycleInfoServiceImpl.findExistEmailCount(mailUrl);
		List<JSONObject> newlist = new ArrayList<JSONObject>();
		Map<JSONObject, String> maplist = new HashMap<JSONObject, String>();
		BillLogEntity billLoggerEntity = logMap.get(accountid_phoneId);
    	
		//分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
		
    	if(null != folder_count && !folder_count.isEmpty()){
    		Set<Entry<String, String[]>> entrys = folder_count.entrySet();
    		try {
	    		for (Entry<String, String[]> info : entrys){
	    			sbStr.setLength(0); // 清理字符串缓冲区
	    			String fid = info.getKey();
	    			String[] count_name = info.getValue();
	    			// 各文件夹中邮件信息 --begin
	    			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfolder_name:{} \tmail_count:{} \tfid:{}", new Object[]{mailUrl, accountId, phoneId, count_name[1], count_name[0], fid});
	    			sbStr.append("<object><int name=\"fid\">");
	    			sbStr.append(fid);
	    			sbStr.append("</int><string name=\"order\">receiveDate</string><string name=\"desc\">1</string><int name=\"start\">1</int><int name=\"total\">");
	    			sbStr.append(count_name[0]);
	    			sbStr.append("</int><string name=\"topFlag\">top</string><int name=\"sessionEnable\">2</int></object>");
	    			lr = hc.postXML(MessageFormat.format(FOLDER_URL, new Object[]{sid, cguid}), sbStr.toString(), set139Floder(sid, cguid, rnd, mtime));
	    			// 各文件夹中邮件信息  --end
	    			String  jsonMail = lr.getHtml();
	    			
	    			JSONObject mails_json;
					mails_json = new JSONObject(jsonMail);
	            	String mailArr = mails_json.getString("var");
	            	JSONArray mail_jsonArray = new JSONArray(mailArr);
	            	if (null != mail_jsonArray){
	            		int length = mail_jsonArray.length();
	            		for (int i = 0; i < length; i++) {
	            			JSONObject object = mail_jsonArray.getJSONObject(i);
	            			String subject = object.getString("subject"); 			// 主題
	            			String from = object.getString("from"); 				// 发件人邮箱
	            			String to = object.getString("to"); 					// 收件人邮箱
	            			String receiveDate = object.getString("receiveDate"); 	// 接收时间
	            			// 按照主题过滤关键字
	            			if(new HttpTools().httpSearch(subject)){
	            				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tquerying:{} e-mail",new Object[]{mailUrl, accountId, phoneId, ++count});
	            				this.send("A1`正在查询第：" + count +"封邮件", key);
	            				String[] path = mailAnalyze.getPath(subject, chengMailUrl(to), conversionDate(receiveDate, formatter),
	            						chengMailUrl(from), mailUrl);
	            				/*if (!StringUtils.isBlank(map.get(path[0]))){
	            					map.remove(path[0]);
	            				}else{
	            					newlist.add(mail_jsonArray.getJSONObject(i));
	            					maplist.put(mail_jsonArray.getJSONObject(i), fid);
	            				}*/
	            				if (StringUtils.isBlank(map.get(path[0]))){ // 库中无此记录,是为新发现的疑似账单
	            					newlist.add(mail_jsonArray.getJSONObject(i));
	            					maplist.put(mail_jsonArray.getJSONObject(i), fid);
	            				}
	            			}
	            		}
	            	}
	    		}
	    		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfilter_mail_size:{}", new Object[]{mailUrl, accountId, phoneId, count});
    		} catch (JSONException e) {
				throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE, ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
			}
    		
    		//发现是否有新账单，有的话进行下载解析，否则返回给用户没有发现新账单信息
            int size = newlist.size();
            //将过滤出的条数存储到操作日志
            if (billLoggerEntity != null){
            	billLoggerEntity.setFiltersNumber(size);
            }
			if (0 != size){
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfound_new_bill_size:{}", new Object[]{mailUrl, accountId, phoneId, size});
				this.send("A1`发现有："+ size +"封新疑似邮件", key);
    			CountDownLatch downLatch = new CountDownLatch(size);
    			// 解析所有邮件
    			try{
	    			for(int j = 0; j < size; j++){
	    				JSONObject object = newlist.get(j);
	    				String subject = object.getString("subject"); 			// 主題
	    				String from = object.getString("from"); 				// 发件人邮箱
	    				String to = object.getString("to"); 					// 收件人邮箱
	    				String mid = object.getString("mid"); 					// mail_id
	    				String receiveDate = object.getString("receiveDate"); 	// 接收时间
	    				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tstart_processing_mail:{}", new Object[]{mailUrl, accountId, phoneId, j+1});
						this.send("B1`正在解析第：" + (j+1) +"/" + size +"封邮件", key);
						// [6]. 获取每个邮件的相关信息,如：获取主题、时间、收发人信息、邮件内容
						String mailFrom = chengMailUrl(from); 		// 发件人邮箱
						String mailTo = chengMailUrl(to); 			// 收件人邮箱
						String formatterDate = conversionDate(receiveDate, formatter); // 接收邮件时间 
						log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tmid:{} \tsubject:{} \tmailFrom:{} \tmailTo:{} \tformatterDate:{} \tmailUrl:{}",
							new Object[]{mailUrl, accountId, phoneId, mid, subject, mailFrom, mailTo, formatterDate, mailUrl}); 			
						// 根据mailid获取邮件主体内容
						String fid = maplist.get(newlist.get(j));
						lr = hc.get(MessageFormat.format(GET_MAIL_INFO, new Object[]{sid, cguid, mid, fid}), set139GetMailInfo(sid, cguid, rnd, mtime));
						StringBuffer content = new StringBuffer();	
						content.append(lr.getHtml());
						// [7]. 调用下载、解析、数据格式化公用方法
						taskExecutor.execute(new ThreadRunAbleTest(parseMailService, downLatch, subject, mailTo, content, formatterDate, mailFrom, mailUrl, count, key, accountid_phoneId, logMap, size, scVersion));
	    			}
	    			downLatch.await();
				} catch (JSONException e) {
					throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE, ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
				} catch (InterruptedException e) {
					MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INTERRUPTED_EXCEPTION_CODE, ErrorCodeContants.INTERRUPTED_EXCEPTION.getMsg(), log);
				}
            }else{
            	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} not found new mail bill", new Object[]{mailUrl, accountId, phoneId});
    			this.send("A1`没发现新疑似邮件！", key);
    		}
    		
    	} else {
    		// 要检索的文件中没邮件  do nothing
    		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} no mail", new Object[]{mailUrl, accountId, phoneId});
    	}
    	
    	this.send("NUMBER`&" + mailUrl + "&" + count, key); // TODO 配合测试用例, 测完要删掉
    	
    }
    
    /**
     * 获取所有文件夹信息
     * 
     * @param hc
     * @param lr
     * @param sid
     * @param cguid
     * @param rnd
     * @param mtime
     * @param sbStr
     * @return Map<String, String[]> 文件夹主键--String[0] 邮件数目 ,String[1]名字
     * @throws MailBillException 
     * @throws JSONException
     */
	
	public Map<String, String[]> getAllFlodersInfo(HttpClientHelper hc,
			HttpResult lr, String sid, String cguid, String rnd, String mtime,
			StringBuffer sbStr) throws MailBillException {
    	// [4]. 收集文件夹相关信息
    	Map<String, String[]> folder_count = new HashMap<String, String[]>();
    	sbStr.setLength(0);
    	sbStr.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><object><int name=\"command\">1</int></object>");
    	lr = hc.postXML(MessageFormat.format(ALL_FOLDERS_URL, new Object[]{sid, cguid}), sbStr.toString(), set139AllFloders(sid, cguid, rnd, mtime));
    	String jsonFolders = lr.getHtml();
    	JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonFolders);
	    	String avrArr = jsonObject.getString("var");
	    	JSONArray jsonArray = new JSONArray(avrArr);
	    	// 1 收件箱; 2 草稿箱; 3 已发送; 4 已删除; 5 垃圾箱; 11 广告文件夹; 15 重要任务; 16 紧急任务;
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonobject1 = jsonArray.getJSONObject(i);
				String fid = jsonobject1.getString("fid");
				String name = jsonobject1.getString("name");
				if (null != fid && !fid.equals("2") && !fid.equals("3")&& !fid.equals("4") 
						&& !fid.equals("5")&& !fid.equals("11") && !fid.equals("15")
						&& !fid.equals("16")) {
					JSONObject jsonObject2 = jsonobject1.getJSONObject("stats");
					String messageCount = jsonObject2.getString("messageCount");
					String[] arr = new String[2];
					arr[0] = messageCount;
					arr[1] = name;
					folder_count.put(fid, arr); // 收件箱及自定义文件夹，主键-邮件数目及名字
				} else {
					// 2 草稿箱; 3 已发送; 4 已删除; 5 垃圾箱; 15 重要任务; 16 紧急任务;
					// do nothing
				}
			}
		} catch (JSONException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE, ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
		}
    	
    	return folder_count;
    }
    
    /**
     * 设置邮箱转发规则
     * 
     * @param hc
     * @param lr
     * @param sid
     * @param cguid
     * @param sbStr
     * @param forwardMail	转发至指定邮箱
     */
    
	public boolean setMailFilters(HttpClientHelper hc, HttpResult lr, String sid, String cguid, 
    		StringBuffer sbStr, String forwardMail, String mailUrl){
    	boolean flag = false;
    	// [3.1]. 所有已经添加的转发规则 
    	sbStr.setLength(0);
    	sbStr.append("<object><int name=\"filterFlag\">0</int><int name=\"extContentFlag\">1</int></object>");
    	lr = hc.postXML(MessageFormat.format(RULE_LIST_URl, new Object[]{sid, cguid}), sbStr.toString(), set139AllFilters(sid));
    	try{
	    	String[] fliterArr = FORWARD_KEY.split(",");
	    	int length = fliterArr.length;
			for(int k = 0; k < length; k++){
	    		if (checkHasFliters(lr.getHtml(), fliterArr[k], forwardMail)){
	        		// [3.2]. 添加新的转发规则
	            	sbStr.setLength(0);
	            	sbStr.append("<object><array name=\"items\"><object><string name=\"opType\">add</string><string name=\"name\">cx</string><int name=\"ignoreCase\">1</int>");
	            	sbStr.append("<int name=\"conditionsRelation\">1</int><int name=\"dealHistoryMail\">0</int><int name=\"rulePiority\">1</int><int name=\"filterId\">-1</int>");
	            	sbStr.append("<string name=\"dealType\">3</string><int name=\"forwardBakup\">1</int><string name=\"replayContent\">您的来信已收到，我会尽快回信。</string>");
	            	sbStr.append("<string name=\"forwardAddr\">").append(forwardMail).append("</string>");
	            	sbStr.append("<string name=\"subject\">").append(fliterArr[k]).append("</string>");
	            	sbStr.append("<int name=\"subjectType\">1</int><int name=\"sortId\">2</int></object></array></object>");
	            	lr = hc.postXML(MessageFormat.format(RULE_POST_URl, new Object[]{sid, cguid}), sbStr.toString(), set139SetFilter(sid));
	            	log.info("e_mail:"+mailUrl+"\tset filter:" + (lr.getHtml().contains("S_OK")? "fliter success":" fliters error"));
	        	} else {
	        		log.info("e_mail:"+mailUrl+" fliter_exists, \tkeyWord:" + fliterArr[k] + " \tforwardMail:" + forwardMail + " no add action");
	        	}
	    	}
	    	flag = true;

    	} catch (Exception e) {
    		flag = false;
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.ADD_FORWARD_RULES_FAILED_CODE, ErrorCodeContants.ADD_FORWARD_RULES_FAILED.getMsg(), log);
		}
    	
    	return flag;
    }
    
    /**
     * 检查是否添加转发规则
     * 
     * @param fliters 已有的转发规则
     * @return boolean	ture 要添加; false 不用添加 
     */
    public boolean checkHasFliters(String fliters, String keyWord, String forwardMail){
    	boolean flag = true;
    	if(StringUtils.isNotBlank(fliters) && fliters.indexOf(keyWord) != -1 && fliters.indexOf(forwardMail) != -1){
    		flag = false;
    	} 
    	return flag;
    }
    
    /**
     * 获取随机数和时间两个参数
     * 
     * @param location	待截取的串
     * @return parArr[0]:rnd;  parArr[1]:mtime
     */
	
	public String[] getRnd_Mtime(String location, String key, String mailUrl) throws MailBillException{
		String[] parArr = new String[2];
		String rnd = "";
		String mtime = "";
		try{
			int rnd_i = location.indexOf("&rnd=");
			int tab_i = location.indexOf("&tab=");
			rnd = location.substring(rnd_i + 5, tab_i);
		} catch (Exception ex){  // 输入邮箱或密码有误
			log.error("e_mail:{} login failed, please check your email user name and password", mailUrl);
        	this.send("A1`登录失败，请检查您的邮箱用户名与密码", key);
        	throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE, ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
		}
		
		int mtime_i = location.indexOf("&mtime=");
		mtime = location.substring(mtime_i + 7, location.length());
		// 组装
		parArr[0] = rnd;
		parArr[1] = mtime;
		return parArr;
	}
    
    /**
     * 获取cguid参数
     */
	public String getCguid(String responseUrl) {
		String cguid = "";
		if (null != responseUrl && !responseUrl.isEmpty()) {
			if (responseUrl.indexOf("cguid=") != -1) {
				int cguid_begin = responseUrl.indexOf("cguid=");
				cguid = responseUrl.substring(cguid_begin + 6, cguid_begin + 19);
			} else {
				log.error("method:{} \tservice:{} cguid参数项不存在",new Object[]{"getCguid", this.getClass()});
			}
		} else {
			log.error("method:{} \tservice:{} responseurl is null", new Object[]{"getCguid", this.getClass()});
		}

		return cguid;
	}
    
    /**
     * 设置139登录header信息
     */
    public Header[] set139LoginHeader() {

    	Header[] result = {
        		new BasicHeader("Host","mail.10086.cn"),
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("DNT","0"),
                new BasicHeader("Referer","http://mail.10086.cn/"),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }
    
    /**
     * 设置获取所有文件列表header信息
     */
    public Header[] set139AllFloders(String sid, String cguid, String rnd, String mtime){
    	
    	Header[] result = {
        		new BasicHeader("Host","appmail.mail.10086.cn"),
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("DNT","0"),
                new BasicHeader("Content-Type","application/xml; charset=UTF-8"),
                new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/index.html?sid="+sid+"&rnd="+rnd+"&tab=&comefrom=54&cguid="+cguid+"&mtime="+mtime),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }
    
    /**
     * 设置获取每个文件列表header信息
     */
    public Header[] set139Floder(String sid, String cguid, String rnd, String mtime){
    	Header[] result = {
        		new BasicHeader("Host","appmail.mail.10086.cn"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
        		new BasicHeader("Accept-Encoding","gzip, deflate"),
        		new BasicHeader("DNT","0"),
        		new BasicHeader("Content-Type","application/xml; charset=UTF-8"),
        		new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/index.html?sid="+sid+"&rnd="+rnd+"&tab=&comefrom=54&cguid="+cguid+"&mtime="+mtime),
        		new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }
    
    /**
     * 设置获取y邮件内容的header信息
     */
    public Header[] set139GetMailInfo(String sid, String cguid, String rnd, String mtime){
    	
    	Header[] result = {
        		new BasicHeader("Host","appmail.mail.10086.cn"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
        		new BasicHeader("Accept-Encoding","gzip, deflate"),
        		new BasicHeader("DNT","0"),
        		new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/index.html?sid="+sid+"&rnd="+rnd+"&tab=&comefrom=54&cguid="+cguid+"&mtime="+mtime),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }
    
    /**
     * 获取所有转发规则header信息
     */
    public Header[] set139AllFilters(String sid){
    	
    	Header[] result = {
        		new BasicHeader("Host","appmail.mail.10086.cn"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
        		new BasicHeader("Accept-Encoding","gzip, deflate"),
        		new BasicHeader("DNT","0"),
        		new BasicHeader("Content-Type", "application/xml; charset=UTF-8"),
        		new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/set/sort_new.html?sid=" + sid),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }

    /**
     * 设置转发规则header信息
     */
    public Header[] set139SetFilter(String sid){
    	
    	Header[] result = {
        		new BasicHeader("Host","appmail.mail.10086.cn"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
        		new BasicHeader("Accept-Encoding","gzip, deflate"),
        		new BasicHeader("DNT","0"),
        		new BasicHeader("Content-Type", "application/xml; charset=UTF-8"),
        		new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/set/create_sort.html?type=normalMail&sid=" + sid),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }
    
   /**
    * 退出邮箱header信息
    */
    public Header[] set139LogoutMail(String sid, String cguid){
    	
    	Header[] result = {
        		new BasicHeader("Host","mail.10086.cn"),
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("DNT","0"),
                new BasicHeader("Referer","http://appmail.mail.10086.cn/m2012/html/index.html?sid="+sid+"&rnd=183&tab=&comefrom=54&cguid="+cguid+"&mtime=120"),
                new BasicHeader("Connection","keep-alive")
        };
    	
        return result;
    }

    /**
     * 退出邮箱header信息
     */
    public Header[] set139GetVcodeImg(){
     	
    	Header[] result = {
				new BasicHeader("Host","imagecode.mail.10086.cn"),
				new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0"),
				new BasicHeader("Accept","image/webp,*/*;q=0.8"),
				new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
				new BasicHeader("Accept-Encoding","gzip, deflate"),
				new BasicHeader("Connection","keep-alive")
        };
     	
        return result;
    }
}
