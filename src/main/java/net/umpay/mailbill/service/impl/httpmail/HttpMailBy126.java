package net.umpay.mailbill.service.impl.httpmail;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * http方式抓取126邮箱模板
 * 
 * @author ycj
 */
@Service
public class HttpMailBy126 extends MyWebSocket implements IHttpMail{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpMailBy126.class);
	
	@Autowired
	private ParseMailService parseMailService;
	@Autowired
	private MailAnalyze mailAnalyze;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor ;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	
	// 登陆页面链接
	private final String SESSION_INIT = "http://mail.126.com";
	// 登录请求链接
    private final String LOGIN_URL = "https://mail.126.com/entry/cgi/ntesdoor?&df=mail126_letter&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&product=mail126&verifycookie=-1&net=failed&style=-1&race=-2_-2_-2_db&uid=";
    // 转发规则列表请求链接
    private final String RULE_LIST_URl = "http://mail.126.com/js5/s?sid={0}&func=user:getMailRules&from=nav&group=options-0&id=NaN&action=click";
    // 转发规则名称
    private final String RULE_NAME = Constants.RULE_NAME;
    // 添加转发规则请求链接
    private final String RULE_ADD_URl = "http://twebmail.mail.126.com/js5/s?sid={0}&func=user:addMailRules";
  	// 转发规则关键字
  	private final String FORWARD_KEY = Constants.FORWARD_KEY;
    
  	/**
  	 * {@inheritDoc}
  	 */
  	@Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_126_TYPE;
	}
  	
    /**
     * 126邮箱http方式抓取邮件
     */
	@Override
    public void httpScan(String mailUrl, String password, String forwardMail, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap) throws MailBillException {
    	HttpClientHelper hc = new HttpClientHelper(true);
        HttpResult lr = hc.get(SESSION_INIT);// 目的是得到 csrfToken 类似
        
        // 拼装登录信息
        Map<String, String> data = new HashMap<String, String>();
        data.put("url2", "http://mail.126.com/errorpage/err_126.htm");
        data.put("savelogin", "0");
        data.put("username", mailUrl);
        data.put("password", password);
        lr = hc.post(LOGIN_URL, data,setHeaderToLogin());// 执行登录
        
        BillLogEntity billLoggerEntity = logMap.get(accountid_phoneId);
        
        //若条件成立 则说明该账号近期存在异常登录情况 需用户输入验证码解除该状态
        if(lr.getHtml().contains("relogin")){
        	this.send("A1`请输入验证码...", key);
        	Document doc = Jsoup.parse(lr.getHtml());
        	String reloginUrl = doc.select("form").attr("action");
        	//执行post登录请求 进入解除异常状态页面
        	lr = hc.post(reloginUrl,data,setHeaderToRelogin(mailUrl));
        	//获取验证码
        	String sysVerifyID = getVcodeImg(lr, hc, mailUrl, key);
        	//刷新验证码
        	sysVerifyID = refreshVcode(lr, hc, mailUrl, sysVerifyID, key);
        	//输入验证码 判断是否成功
        	lr = relogin(data, lr, hc, sysVerifyID, mailUrl, password, key);
        	while(!lr.getHtml().contains("200")){
        		this.send("A1`登录失败，请重新输入验证码...", key);
        		sysVerifyID = getVcodeImg(lr, hc, mailUrl, key);
            	sysVerifyID = refreshVcode(lr, hc, mailUrl, sysVerifyID, key);
        		lr = relogin(data, lr, hc, sysVerifyID, mailUrl, password, key);
        		if(lr.getHtml().contains("sid")){
        			break;
        		}
        	}
        }
        //分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
       
		if(!lr.getHtml().contains("http://mail.126.com/errorpage/err_126.htm?")){	//判断是否登陆成功
        	if (billLoggerEntity != null){
        		billLoggerEntity.setLogOn(MailBillTypeConstants.SUCCESS);
        	}
        	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} http login success", new Object[]{mailUrl, accountId, phoneId});
        	this.send("A1`登陆成功...",key);
        	
        	// 从序列中获取一个long值作为版本号
			Long scVersion = billCycleInfoServiceImpl.findscVersionSEQ();
			
        	//获取sid
			Document doc = Jsoup.parse(lr.getHtml());
			String sid = doc.select("script").html().split("=")[2];
			sid = sid.substring(0,sid.length()-2);
			
			//添加转发规则 TODO 暂时关闭转发功能
            /*boolean forwardRules = forwardRules(data, lr, hc, sid, mailUrl, forwardMail);
            if (billLoggerEntity != null){
        		billLoggerEntity.setForwardResults(forwardRules ? MailBillTypeConstants.SUCCESS : MailBillTypeConstants.FAILED);
        	}*/
			//获取ssid
            lr = hc.get("http://mail.126.com/js5/main.jsp?sid="+sid+"&df=mail126_letter", setHeaderToGetSsid());
            Document docMain = Jsoup.parse(lr.getHtml());
            Element ssidElement = docMain.select("span[id=spnCheckFont]").first().nextElementSibling();
            String ssid = ssidElement.html().substring(ssidElement.html().indexOf("ssid=")+5,ssidElement.html().indexOf("',host:"));
			
            //获取用户的收件箱及自定义邮箱邮件数目 
            List<String> mailboxIdList = new ArrayList<String>();
            data.clear();
            data.put("var","<?xml version=\"1.0\"?><object><array name=\"items\"><object><string name=\"func\">mbox:getAllFolders</string><object name=\"var\"><boolean name=\"stats\">true</boolean><boolean name=\"threads\">false</boolean></object></object><object><string name=\"func\">mbox:getFolderStats</string><object name=\"var\"><array name=\"ids\"><string>1,3</string><string>3</string></array><boolean name=\"messages\">true</boolean><boolean name=\"threads\">false</boolean></object></object><object><string name=\"func\">mbox:listTags</string><object name=\"var\"><boolean name=\"stats\">true</boolean><boolean name=\"threads\">false</boolean></object></object></array></object>");
            lr = hc.post("http://twebmail.mail.126.com/js5/s?sid="+sid+"&func=global:sequential&from=nav&group=folder&id=1&action=click&mboxentry=1&welc=rcmdtab1&deftabclick=t0",data,setHeaderGetMailCount(sid));	//点击收信按钮产生的请求
            String[] mailboxInfo = lr.getHtml().split("unreadMessageCount");
            String mailboxTemp = "";
            this.send("A1`正在过滤疑似账单邮件...", key);
            int siz = mailboxInfo.length-3;
			for(int i = 0; i < siz;  i++){	//后三个元素无需遍历
            	if(!mailboxInfo[i].contains("'system':true") || mailboxInfo[i].contains("收件箱")){
            		mailboxTemp = mailboxInfo[i].substring(mailboxInfo[i].indexOf("id")+4,mailboxInfo[i].indexOf("name")-3);
            		mailboxTemp += ":"+mailboxInfo[i].substring(mailboxInfo[i].indexOf("messageCount")+14,mailboxInfo[i].indexOf("messageSize")-3);
            		mailboxIdList.add(mailboxTemp);
            	}
            }
            log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfolder_id_of_mail_number:{}", new Object[]{mailUrl, accountId, phoneId, mailboxIdList.toString()});
			
            //获取各邮箱中各邮件的信息
            String fid;			//邮箱id
            String mailcount;	//邮箱中的邮件数
            String mid;			//每封邮件的id
            String subject;		//主题
			String senderAdd;	//发件人
			String receiveAdd;	//收件人
			String sentDate;	//发送时间
			int year;			//年
			int month;			//月
			int day;			//日
			int hour;			//时
			int min;			//分
			int seconds;		//秒
			int count = 0;		//统计账单邮件数
			
			Map<String, String> map = billCycleInfoServiceImpl.findExistEmailCount(mailUrl);
			List<String> newlist = new ArrayList<String>();
			
            int si = mailboxIdList.size();
			for(int i = 0; i < si; i++){
            	fid = mailboxIdList.get(i).substring(0,mailboxIdList.get(i).indexOf(":"));
            	mailcount = mailboxIdList.get(i).substring(mailboxIdList.get(i).indexOf(":")+1,mailboxIdList.get(i).length());
            	if(!mailcount.equals("0")){
            		data.clear();
            		data.put("var", "<?xml version=\"1.0\"?><object><int name=\"fid\">"+fid+"</int><boolean name=\"skipLockedFolders\">false</boolean><string name=\"order\">date</string><boolean name=\"desc\">true</boolean><int name=\"start\">0</int><int name=\"limit\">"+mailcount+"</int><boolean name=\"topFirst\">true</boolean><boolean name=\"returnTotal\">true</boolean><boolean name=\"returnTag\">true</boolean></object>");
            		lr = hc.post("http://twebmail.mail.126.com/js5/s?sid="+sid+"&func=mbox:listMessages&from=nav&group=folder&id=1&action=click&mboxentry="+fid, data, setHeaderToGetMid(sid));
            		String[] mailInfo = lr.getHtml().split("backgroundColor");
            		int j2 = mailInfo.length-1;
					for(int j = 0; j < j2; j++){
            			mid = mailInfo[j].substring(mailInfo[j].indexOf("'id'")+6,mailInfo[j].indexOf("'fid'")-3);
            			subject = mailInfo[j].substring(mailInfo[j].indexOf("'subject'")+11,mailInfo[j].indexOf("'sentDate'")-3);
            			senderAdd = mailInfo[j].substring(mailInfo[j].indexOf("'from'")+8,mailInfo[j].indexOf("'to'")-3);
            			receiveAdd = mailInfo[j].substring(mailInfo[j].indexOf("'to'")+6,mailInfo[j].indexOf("'subject'")-3);
            			String[] sentDataArray = mailInfo[j].substring(mailInfo[j].indexOf("'sentDate':new Date(")+20,mailInfo[j].indexOf("'receivedDate'")-3).split(",");
            			year = Integer.parseInt(sentDataArray[0]);
            			month = Integer.parseInt(sentDataArray[1]);
            			day = Integer.parseInt(sentDataArray[2]);
            			hour = Integer.parseInt(sentDataArray[3]);
            			min = Integer.parseInt(sentDataArray[4]);
            			seconds = Integer.parseInt(sentDataArray[5]);
            			
            			@SuppressWarnings("deprecation")
						Date date = new Date(year-1900,month,day,hour,min,seconds);
            			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm");
            			sentDate = formatter.format(date);
            			
            			//根据mid读取邮件获取邮件主体内容
            			if(new HttpTools().httpSearch(subject)){
            				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tquerying:{} e-mail",new Object[]{mailUrl, accountId, phoneId, ++count});
            				this.send("A1`正在查询第：" + count +"封邮件", key);
            				String[] path = mailAnalyze.getPath(subject, receiveAdd, sentDate, senderAdd, mailUrl);
            				if (StringUtils.isBlank(map.get(path[0]))){ // 库中无此记录,是为新发现的疑似账单
            					newlist.add(mailInfo[j]);
            				}
            			}
            		}
            	}
            }
			this.send("NUMBER`&" + mailUrl + "&" + count, key); // TODO 配合测试用例, 测完要删掉
			
			//退出
            lr = hc.get("http://www.126.com/logout.htm");
            
            int size = newlist.size();
            //将过滤出的条数存储到操作日志
            if (billLoggerEntity != null){
            	billLoggerEntity.setFiltersNumber(size);
            }
			if (0 != size){
            	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tfound_new_bill_size:{}", new Object[]{mailUrl, accountId, phoneId, size});
    			this.send("A1`发现有："+ size +"封新疑似邮件", key);
    			//预设置子线程数目
    			CountDownLatch latch = new CountDownLatch(size);
    			// 解析所有邮件
    			for(int j = 0; j < size; j++){
        			mid = newlist.get(j).substring(newlist.get(j).indexOf("'id'")+6,newlist.get(j).indexOf("'fid'")-3);
        			subject = newlist.get(j).substring(newlist.get(j).indexOf("'subject'")+11,newlist.get(j).indexOf("'sentDate'")-3);
        			senderAdd = newlist.get(j).substring(newlist.get(j).indexOf("'from'")+8,newlist.get(j).indexOf("'to'")-3);
        			receiveAdd = newlist.get(j).substring(newlist.get(j).indexOf("'to'")+6,newlist.get(j).indexOf("'subject'")-3);
        			String[] sentDataArray = newlist.get(j).substring(newlist.get(j).indexOf("'sentDate':new Date(")+20,newlist.get(j).indexOf("'receivedDate'")-3).split(",");
        			year = Integer.parseInt(sentDataArray[0]);
        			month = Integer.parseInt(sentDataArray[1]);
        			day = Integer.parseInt(sentDataArray[2]);
        			hour = Integer.parseInt(sentDataArray[3]);
        			min = Integer.parseInt(sentDataArray[4]);
        			seconds = Integer.parseInt(sentDataArray[5]);
        			
        			@SuppressWarnings("deprecation")
					Date date = new Date(year-1900,month,day,hour,min,seconds);
        			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm");
        			sentDate = formatter.format(date);
        			
        			//根据mid读取邮件获取邮件主体内容
    				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tstart_processing_mail:{}", new Object[]{mailUrl,accountId, phoneId, j+1});
    				this.send("B1`正在解析第：" + (j+1) + "/" + size +"封邮件", key);
    				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \tsubject:{} \tthe_sender: {} \taddressee：{} \tsend_time：{}", 
    						new Object[]{mailUrl, accountId, phoneId, subject, senderAdd, receiveAdd, sentDate});
    				lr = hc.get("http://twebmail.mail.126.com/js5/read/readhtml.jsp?ssid="+ssid+"&mid="+mid+"&color=003399&font=15", setHeaderToReadmail(sid));
    				StringBuffer content = new StringBuffer();	//邮件主体内容
    				content.append(lr.getHtml());
    				taskExecutor.execute(new ThreadRunAbleTest(parseMailService, latch, subject, receiveAdd, content, sentDate, senderAdd, mailUrl, count, key, accountid_phoneId, logMap, size, scVersion));
    			}
    			try {
					latch.await();
				} catch (InterruptedException e) {
					MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INTERRUPTED_EXCEPTION_CODE, ErrorCodeContants.INTERRUPTED_EXCEPTION.getMsg(), log);
				}
    		}else{
    			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} not found new mail bill", new Object[]{mailUrl, accountId, phoneId});
    			this.send("A1`没发现新疑似邮件", key);
    		}
        } else{
        	if (billLoggerEntity != null){
        		billLoggerEntity.setLogOn(MailBillTypeConstants.FAILED);
        	}
        	log.error("e_mail:{} \taccount_id:{} \tphone_id:{} login failed, please check your email user name and password", new Object[]{mailUrl, accountId, phoneId});
        	this.send("A1`登录失败，请检查您的邮箱用户名与密码", key);
        	throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE, ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
        }
    }
    
    /**
     * 添加转发规则
     * 
     * @param data	表单数据map
     * @param lr	结果集
     * @param hc	httpclient
     * @param sid	sid
     * @param mailUrl	用户email地址
     * @param forwardTo	转发至地址
     */
    
	public boolean forwardRules(Map<String, String> data, HttpResult lr, HttpClientHelper hc, String sid, String mailUrl, String forwardTo){
    	boolean flag = false;
    	//查询用户的来信列表中是否包含“银信宝”字样	 有则不再进行转发规则的添加
		data.clear();
		data.put("var", "<?xml version=\"1.0\"?><object/>");
		lr = hc.post(MessageFormat.format(RULE_LIST_URl, sid),data, setHeaderTORuleList(sid));
		try{
			//转发
			if(!lr.getHtml().contains(RULE_NAME) && lr.getHtml().contains("'code':'S_OK'")){
				log.info("e_mail:{} \tadd_forwarding_rules:{}", new Object[]{mailUrl, RULE_NAME});
				String[] fowardKeyArray = FORWARD_KEY.split(",");
				int length = fowardKeyArray.length;
				for(int i = 0; i < length; i++ ){
					data.clear();
					data.put("var", "<?xml version=\"1.0\"?><object><array name=\"items\"><object><string name=\"name\">"+RULE_NAME+"</string><boolean name=\"disabled\">false</boolean><boolean name=\"continue\">true</boolean><array name=\"condictions\"><object><boolean name=\"disabled\">false</boolean><string name=\"field\">subject</string><string name=\"operand\">"+fowardKeyArray[i]+"</string><boolean name=\"ignoreCase\">true</boolean><string name=\"operator\">contains</string></object><object><string name=\"field\">accounts</string><string name=\"operator\">contains</string><array name=\"operand\"><string>"+mailUrl+"</string></array></object></array><array name=\"actions\"><object><string name=\"type\">forward</string><string name=\"target\">"+forwardTo+"</string><boolean name=\"keepLocal\">true</boolean></object></array></object></array></object>");
					lr = hc.post(MessageFormat.format(RULE_ADD_URl, sid),data, setHeaderTORuleAdd(sid));
			    }
			    log.info("e_mail:{} add filters success", mailUrl);
			    flag = true;
			}else{
				flag = true;
				log.info("e_mail:{} \tforwarding rule:{} exists",new Object[]{mailUrl, RULE_NAME});
			}
		}catch (Exception e) {
			flag = false;
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.ADD_FORWARD_RULES_FAILED_CODE, ErrorCodeContants.ADD_FORWARD_RULES_FAILED.getMsg(), log);
		}
		
		return flag;
    }
    
    // -------------- private function -------------
    
    /**
     * 获取图片验证码
     * 
     * @param lr	结果集
     * @param hc	httpclient
     * @param mailUrl	用户email地址
     * @param key	
     */
	private String getVcodeImg(HttpResult lr, HttpClientHelper hc, String mailUrl,String key){
		//请求验证码前 需获取一个id 
    	lr = hc.get("https://reg.163.com/services/getid", setHeaderToReloginId(mailUrl));
    	String sysVerifyID = lr.getHtml();
    	//请求图片验证码
    	lr = hc.get("https://reg.163.com/services/getimg?id="+sysVerifyID, setHeaderToGetimg(mailUrl));
//    	this.send("D1`验证码url为：data:image/jpg;base64,"+Base64.encode(lr.getResponse()),key);
    	this.send("I1`验证码url为："+lr.getResponseUrl(),key);
		return sysVerifyID;
	}
	
	/**
     * 刷新验证码
     * 
     * @param lr	结果集
     * @param hc	httpclient
     * @param mailUrl
     * @param sysVerifyID
     * @param key	
     */
	
	@SuppressWarnings("static-access")
	private String refreshVcode(HttpResult lr, HttpClientHelper hc, String mailUrl, String sysVerifyID, String key){
		while(true){
    		String code = super.vcodeMap.get(key);
        	while(code == null){
        		code = super.vcodeMap.get(key);
        	}
        	if(code.contains("RI1`")){
        		break;
        	}
        	if(code.contains("refreshVcode")){
        		sysVerifyID = getVcodeImg(lr, hc, mailUrl, key);
        		this.vcodeMap.remove(key);
        	}
    	}
		return sysVerifyID;
	}
	
	/**
     * relogin登录，通过验证码解除账号异常登录状态
     * 
     * @param data	表单数据map
     * @param lr	结果集
     * @param hc	httpclient
     * @param sysVerifyID	
     * @param mailUrl	用户email地址
     * @param password	用户密码
     * @param key	
     */
	
	@SuppressWarnings("static-access")
	private HttpResult relogin(Map<String,String> data, HttpResult lr, HttpClientHelper hc, String sysVerifyID, String mailUrl, String password, String key){
		String code = super.vcodeMap.get(key);
    	while(code == null){
    		code = super.vcodeMap.get(key);
    	}
    	String filledVerifyID = code.substring(code.indexOf("RI1`")+4, code.length()); //验证码
    	
    	//校验验证码
		lr = hc.get("https://reg.163.com/services/checkcode?filledVerifyID="+filledVerifyID+"&sysVerifyID="+sysVerifyID+"&isLoginException=1", setHeaderToCheckcode(mailUrl));
	    this.vcodeMap.remove(key);
    	if(lr.getHtml().contains("200")){
        	log.info("e_mail:{} unwind exception login status of success");
        	data.clear();
        	data.put("url2", "http://mail.126.com/errorpage/err_126.htm");
            data.put("savelogin", "0");
            data.put("username", mailUrl);
            data.put("password", password);
            lr = hc.post(LOGIN_URL, data,setHeaderToLogin());// 执行登录
        }else{
        	log.info("e_mail:{} verification code verification failed, please re input");
        }
		return lr;
	}
    
    // 设置登录请求头信息
    private Header[] setHeaderToLogin() {
        Header[] result = {
                new BasicHeader("User-Agent","Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Cache-Control","no-cache"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded"),
                new BasicHeader("Host","ssl.mail.126.com"),
                new BasicHeader("Referer","http://mail.126.com/"),
                new BasicHeader("Accept","text/html, application/xhtml+xml, */*")
                 
        };
        return result;
    }
    
    // 设置获取sid请求头信息
    private Header[] setHeaderToGetSsid() {
        Header[] result = {
        		new BasicHeader("Host","twebmail.mail.126.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    // 设置获取邮件数请求头信息
    private Header[] setHeaderGetMailCount(String sid) {
        Header[] result = {
        		new BasicHeader("Host","twebmail.mail.126.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/javascript"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Origin","http://twebmail.mail.126.com"),
                new BasicHeader("Referer","http://twebmail.mail.126.com/js5/main.jsp?sid="+sid+"&df=mail126_letter"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    // 设置获取mid请求头信息
    private Header[] setHeaderToGetMid(String sid) {
        Header[] result = {
        		new BasicHeader("Host","twebmail.mail.126.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/javascript"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Origin","http://twebmail.mail.126.com"),
                new BasicHeader("Referer","http://twebmail.mail.126.com/js5/main.jsp?sid="+sid+"&df=mail126_letter"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    // 设置读取邮件请求头信息
    private Header[] setHeaderToReadmail(String sid) {
        Header[] result = {
        		new BasicHeader("Host","twebmail.mail.126.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Referer","http://twebmail.mail.126.com/js5/main.jsp?sid="+sid+"&df=mail126_letter"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    //获取转发规则列表请求头信息
    private Header[] setHeaderTORuleList(String sid) {
        Header[] result = {
        		new BasicHeader("Host","mail.126.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/javascript"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Referer","http://mail.126.com/js5/main.jsp?sid="+sid+"&df=webmailyeah"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Pragma","no-cache"),
                new BasicHeader("Cache-Control","no-cache")
        };
        return result;
    }
    
    //添加转发规则请求头信息
    private Header[] setHeaderTORuleAdd(String sid) {
        Header[] result = {
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8"),
                new BasicHeader("Host","twebmail.mail.126.com"),
                new BasicHeader("Referer","http://twebmail.mail.126.com/js5/main.jsp?sid="+sid+"&df=mail126_letter"),
                new BasicHeader("Accept","text/javascript")
        };
        return result;
    }
    
    //获取relogin时的头信息
    private Header[] setHeaderToRelogin(String username) {
        Header[] result = {
        		new BasicHeader("Host","reg.163.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","text/javascript"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded"),
                new BasicHeader("Origin","https://reg.163.com"),
                new BasicHeader("Referer","https://reg.163.com/login.jsp?username="+username+"&url=http://entry.mail.126.com/coremail/fcg/ntesdoor2"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Pragma","no-cache"),
                new BasicHeader("Cache-Control","no-cache")
        };
        return result;
    }
    
    //获取reloginId头信息
    private Header[] setHeaderToReloginId(String username) {
        Header[] result = {
        		new BasicHeader("Host","reg.163.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","*/*"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","https://reg.163.com/login.jsp?username="+username+"&url=http://entry.mail.126.com/coremail/fcg/ntesdoor2"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("X-Prototype-Version","1.4.0"),
                new BasicHeader("X-Requested-With","XMLHttpRequest")
        };
        return result;
    }
    
    //获取验证码图片头信息
    private Header[] setHeaderToGetimg(String username) {
        Header[] result = {
        		new BasicHeader("Host","reg.163.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","image/webp,*/*;q=0.8"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","https://reg.163.com/login.jsp?username="+username+"&url=http://entry.mail.126.com/coremail/fcg/ntesdoor2"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    //获取校验验证码请求头信息
    private Header[] setHeaderToCheckcode(String username) {
        Header[] result = {
        		new BasicHeader("Host","reg.163.com"), 
                new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
                new BasicHeader("Accept","*/*"),
                new BasicHeader("Accept-Language","zh-CN"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","https://reg.163.com/login.jsp?username="+username+"&url=http://entry.mail.126.com/coremail/fcg/ntesdoor2"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("X-Prototype-Version","1.4.0"),
                new BasicHeader("X-Requested-With","XMLHttpRequest")
        };
        return result;
    }
}
