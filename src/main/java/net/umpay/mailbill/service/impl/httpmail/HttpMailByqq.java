package net.umpay.mailbill.service.impl.httpmail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * http方式抓取qq邮箱模板
 * 
 * @author ycj
 */
@Service
public class HttpMailByqq extends MyWebSocket implements IHttpMail{
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpMailByqq.class);
	
	@Autowired
	private MailAnalyze mailAnalyze;
	@Autowired
	private ParseMailService parseMailService;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor ;
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	private final String SESSION_INIT = "https://mail.qq.com/cgi-bin/loginpage";
    @SuppressWarnings("unused")
	private final String FORWARD_MAIL = "ycj7126168@163.com";
    /**
     * {@inheritDoc}
     */
    @Override
	public String httpScanType() {
		return MailBillTypeConstants.HTTP_SCAN_QQ_TYPE;
	}
    /**
     * {@inheritDoc}
     */
    
	@Override
    public void httpScan(String mailUrl, String password, String forwardMail, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap)throws MailBillException  {
        HttpClientHelper hc = new HttpClientHelper(true);
        HttpResult lr = hc.get(SESSION_INIT);// 目的是得到 csrfToken 类似
        Map<String, String> data = new HashMap<String, String>();
    	
    	BillLogEntity billLoggerEntity = logMap.get(accountid_phoneId);
        if (billLoggerEntity != null){
    		billLoggerEntity.setLogOn(MailBillTypeConstants.SUCCESS);
    	}
        
        //分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
        
        // 获取验证码
        String v_code = null;
        String[] info = new String[3];
        lr = hc.get("https://ssl.ptlogin2.qq.com/check?regmaster=&uin="+mailUrl+"&appid=522005705&js_ver=10082&js_type=1&login_sig=iMVXWYmXWlxcAW1xKV1i479H0C*8FyinZeKVY*Xw2AYumcAjNFkhqY18Dm5wpjqL&u1=https%3A%2F%2Fmail.qq.com%2Fcgi-bin%2Flogin%3Fvt%3Dpassport%26vm%3Dwpt%26ft%3Dloginpage%26target%3D&r=0.37017362822778754", setHeaderToGetVcode());
        log.info("e_mail:{} \taccount_id:{} \tphone_id:{} lr.getHtml : {}",new Object[]{mailUrl, lr.getHtml("gb18030")});
        //若条件成立 验证码可获取 无需输入
        if(lr.getHtml("gb18030").substring(14, 15).equals("0")){
        	v_code = lr.getHtml("gb18030").substring(18, 22);
        	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Verification code ：{}",new Object[]{mailUrl, accountId, phoneId, v_code});
        	info[0] = mailUrl;
        	info[1] = password;
        	info[2] = v_code;
        	lr = login(lr,hc,info,key);
        	if(!lr.getHtml().contains("登录成功")){
        		this.send("A1`登录失败，请检查您的邮箱用户名与密码！", key);
        		log.error("e_mail:{} \taccount_id:{} \tphone_id:{} login failed, please check your email user name and password", new Object[]{mailUrl, accountId, phoneId});
        		throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE, ErrorCodeContants.EMAIL_LOGIN_FAILED.getMsg(), log);
        	}
        }
        //此条件成立 需手动输入验证码进行登录
        if(lr.getHtml("gb18030").substring(14, 15).equals("1")){
        	this.send("A1`请输入验证码...", key);
        	log.error("e_mail:{} \taccount_id:{} \tphone_id:{} need verification code, please enter the verification code", new Object[]{mailUrl, accountId, phoneId});
        	info = getVcode(lr,hc,mailUrl, key);
        	//刷新登录验证码
        	info = refresh(lr, hc, info, mailUrl.substring(0,mailUrl.indexOf("@")), key);
        	lr = login(lr,hc,info,key);
        	while(!lr.getHtml().contains("登录成功")){
        		this.send("A1`登录失败，请检查您输入的信息！", key);
        		log.error("e_mail:{} \taccount_id:{} \tphone_id:{} login failed, please check the information you entered",  new Object[]{mailUrl, accountId, phoneId});
        		info = getVcode(lr,hc,mailUrl, key);
        		info = refresh(lr, hc, info, mailUrl.substring(0,mailUrl.indexOf("@")), key);
        		lr = login(lr,hc,info,key);
        	}
        }
        //从登陆成功的响应中提取链接并访问（此链接响应为lr.getHtml()）
		String new_url = lr.getHtml().substring(lr.getHtml().indexOf("ptuiCB('0','0','")+16,lr.getHtml().indexOf("','1','登录成功！"));
		lr = hc.get(new_url,setHeaderToGetSid());
		
		//判断是否需要独立密码
		lr = ipwdJudge(lr, hc, data, mailUrl, new_url, key);
		
		// 获取sid
		this.send("A1`登录成功！",key);
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} http login success",  new Object[]{mailUrl, accountId, phoneId});
		String sid = null;
		if(lr.getHtml("gb18030").contains("正在登录QQ邮箱") && lr.getHtml("gb18030").contains("debugger")){
			sid = lr.getHtml("gb18030").substring(lr.getHtml("gb18030").indexOf("sid=")+4,lr.getHtml("gb18030").indexOf("debugger")-3);
		}
		if(lr.getHtml("gb18030").contains("正在登录QQ邮箱") && lr.getHtml("gb18030").contains("if (targetUrl == \"\")")){
			sid = lr.getHtml("gb18030").substring(lr.getHtml("gb18030").indexOf("sid=")+4,lr.getHtml("gb18030").indexOf("if (targetUrl == \"\")")-3);
		}
		else{	//无需输入验证码时 sid截取
			sid = lr.getHtml("gb18030").substring(lr.getHtml("gb18030").indexOf("sid=")+4, lr.getHtml("gb18030").indexOf("&url="));
		}
		
		// 从序列中获取一个long值作为版本号
		Long scVersion = billCycleInfoServiceImpl.findscVersionSEQ();
		
		// 获取inbox所有邮件id号
		List<String> mailIdList = new ArrayList<String>(); 			// 记录所有邮件的mailId
		List<String> mailIdListSecond = new ArrayList<String>();	// 记录所有因频率过快需要二次过滤的mailId
		String mailId = "" ; 
		
		lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?folderid=1&page=0&s&sid="+sid+"&nocheckframe=true", setHeaderToGetMailId());
		Document doc = Jsoup.parse(lr.getHtml("gb18030"));
		// 获取inbox邮件列表页数
		Element pageTotal = doc.select("div[class=right]").last();
		int pageSize = Integer.parseInt(pageTotal.child(0).toString().substring(pageTotal.child(0).toString().indexOf("(")+1, pageTotal.child(0).toString().indexOf("+")-1));
		if(pageSize == -1){
			this.send("A1`"+mailUrl+" 的邮箱收件箱中没有邮件！" , key);
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} no mail inbox!",  new Object[]{mailUrl, accountId, phoneId});
		}
		else{
			this.send("A1`正在过滤疑似账单邮件...", key);
			// 遍历inbox全部邮件 完成过滤
			for(int i = 0; i <= pageSize; i++){
				lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?folderid=1&page="+i+"&s&sid="+sid+"&nocheckframe=true", setHeaderToGetMailId());
				Document docInbox = Jsoup.parse(lr.getHtml("gb18030"));
				// 将过滤出的maiId存到list中
				Elements subjects = docInbox.select("u[tabindex=0]");
				for (Element subject : subjects) {
					if(new HttpTools().httpSearch(subject.html())){
						mailId = subject.parent().parent().parent().parent().parent().parent().previousElementSibling().previousElementSibling().select("input[name=mailid]").val();
						//记录非广告邮件的mailId
						if(!mailId.equals("AC0000-00000000000000000000000")){ 
							mailIdList.add(mailId);
						}
					}
				}
			}
		}
		
		// 获取用户自定义文件夹中的账单邮件
		lr = hc.get("http://mail.qq.com/cgi-bin/frame_html?sid="+sid, setHeaderToGetList());
		Document docOther = Jsoup.parse(lr.getHtml("gb18030"));
		if(docOther.toString().contains("我的文件夹")){
			lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?folderid=personal&page=0&s&sid="+sid+"&nocheckframe=true", setHeaderToGetMailId());
			Document docPersonal = Jsoup.parse(lr.getHtml("gb18030"));
			// 获取用户邮件列表页数
			Element pageTotalPersonal = docPersonal.select("div[class=right]").last();
			int pageSizePersonal = Integer.parseInt(pageTotalPersonal.child(0).toString().substring(pageTotalPersonal.child(0).toString().indexOf("(")+1, pageTotalPersonal.child(0).toString().indexOf("+")-1));
			if(pageSizePersonal == -1){
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} no mail in the mailbox user custom!",  new Object[]{mailUrl, accountId, phoneId});
			}
			else{
				// 遍历用户自定义邮箱中的全部邮件 完成过滤
				this.send("A1`正在过滤疑似账单邮件..." , key);
				for(int i = 0; i <= pageSizePersonal; i++){
					lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?folderid=personal&page="+i+"&s&sid="+sid+"&nocheckframe=true", setHeaderToGetMailId());
					Document docInbox = Jsoup.parse(lr.getHtml("gb18030"));
					// 将过滤出的maiId存到list中
					Elements subjects = docInbox.select("u[tabindex=0]");
					for (Element subject : subjects) {
						if(new HttpTools().httpSearch(subject.html())){
							mailId = subject.parent().parent().parent().parent().parent().parent().previousElementSibling().previousElementSibling().select("input[name=mailid]").val();
							mailIdList.add(mailId);
						}
					}
				}
			}
		}
		else{
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} the user does not exist a custom email!",  new Object[]{mailUrl, accountId, phoneId});
		}
		
		// 获取广告邮件中的mailId
		lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?page=0&folderid=1&flag=add&fun=&s=&searchmode=&filetype=&listmode=&stype=&ftype=&AddrID=&grpid=&category=&showattachtag=&from=&sorttype=6&sortasc=0&t=mail_list_ad&flag=add&sid="+sid+"&nocheckframe=true", setHeaderToGetAdMailId());
		Document docAD = Jsoup.parse(lr.getHtml("gb18030"));
		// 获取用户邮件列表页数
		int count_AD = 0;	// 统计 广告邮件AD 中的账单数
		Element pageTotalAD = docAD.select("div[class=right]").last();
		int pageSizeAD = Integer.parseInt(pageTotalAD.child(0).toString().substring(pageTotalAD.child(0).toString().indexOf("(")+1, pageTotalAD.child(0).toString().indexOf("+")-1));
		if(pageSizeAD != -1){	// 判断是否有邮件
			pageSizeAD++;
			this.send("A1`正在过滤疑似账单邮件...", key);
			for(int i = 0; i < pageSizeAD; i++){
				lr = hc.get("http://set2.mail.qq.com/cgi-bin/mail_list?page="+i+"&folderid=1&flag=add&fun=&s=&searchmode=&filetype=&listmode=&stype=&ftype=&AddrID=&grpid=&category=&showattachtag=&from=&sorttype=6&sortasc=0&t=mail_list_ad&flag=add&sid="+sid+"&nocheckframe=true", setHeaderToGetAdMailId());
				Document docPageAD = Jsoup.parse(lr.getHtml("gb18030"));
				Elements subjects = docPageAD.select("u[tabindex=0]");
				for (Element subject : subjects) {
					if(new HttpTools().httpSearch(subject.html())){
						mailId = subject.parent().parent().parent().parent().parent().parent().previousElementSibling().previousElementSibling().select("input[name=mailid]").val();
						mailIdList.add(mailId);
						count_AD++;
					}
				}
			}
		}
		else{
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} no mail users advertising mail in AD!",  new Object[]{mailUrl, accountId, phoneId});
		}
		int size = mailIdList.size();
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} email address: {} user is filtered to bill mail {} letters! {} of these letters in the mail in AD! ",
				new Object[]{mailUrl, accountId, phoneId, mailUrl, size, count_AD});
		
		Map<String, String> map = billCycleInfoServiceImpl.findExistEmailCount(mailUrl);
		List<String> newlist = new ArrayList<String>();
		
		// 遍历过滤到的邮件  
		int count = 0;
		String subject = "";	// 主题
		String senderAdd = "";	// 发件人
		String receiveAdd = "";	// 收件人
		String sentData = "";	// 发送时间
		String year = "";		// 年
		String month = "";		// 月
		String day = "";		// 日
		String week = "";		// 星期
		String hour = "";		// 时 (String)
		int hourInt;			// 时 (int)
		String min = "";		// 分
		String timeSlot = "";	// 时间段
		
		for(int i = 0; i < size; i++){
			mailId = mailIdList.get(i);
			lr = hc.get("http://set2.mail.qq.com/cgi-bin/readmail?folderid=1&folderkey=1&t=readmail&mailid="+mailId+"&mode=pre&maxage=3600&base=12.52&ver=10920&sid="+sid+"#stattime=1405496987884",setHeaderToGetMailDoc(sid));
			Document docMail = Jsoup.parse(lr.getHtml("gb18030"));
			
			if(lr.getHtml("gb18030").contains("您请求的频率太快，请稍后再试")){
				mailIdListSecond.add(mailId);
				continue;
			}
			if(lr.getHtml("gb18030").equals("")){
				continue;
			}
			if(docMail.select("span[id=subject]").first() != null){
				subject = docMail.select("span[id=subject]").first().html();
			}
			if(docMail.select("b[id=tipFromAddr_readmail]").first() != null){
				senderAdd = docMail.select("b[id=tipFromAddr_readmail]").first().attr("fromaddr");
			}
			if(docMail.select("div[class=addrtitle nowrap]").first() != null){
				receiveAdd = docMail.select("div[class=addrtitle nowrap]").first().nextElementSibling().childNode(0).attr("e");
			}
			if(docMail.select("td[class=txt_right settingtable noUnderLineList]").first() != null){
				sentData = docMail.select("td[class=txt_right settingtable noUnderLineList]").first().previousElementSibling().child(1).html();
			}
			
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Querying the {} e-mail.",new Object[]{mailUrl, accountId, phoneId, ++count});
			this.send("A1`正在查询第：" + count +"封邮件", key);
			String[] path = mailAnalyze.getPath(subject, receiveAdd, sentData, senderAdd, mailUrl);
			/*if (!StringUtils.isBlank(map.get(path[0]))){
				map.remove(path[0]);
			}else{
				newlist.add(mailId);
			}*/
			if (StringUtils.isBlank(map.get(path[0]))){ // 库中无此记录,是为新发现的疑似账单
				newlist.add(mailId);
			}
		}
		
		// 因频率过快引起的二次过滤
		if(mailIdListSecond.size() != 0){
			count=reScan(lr,hc,mailIdListSecond,mailId,sid,subject,senderAdd,receiveAdd,sentData,mailUrl,count, key, accountid_phoneId, logMap, map, newlist);
		}
		this.send("NUMBER`&" + mailUrl + "&" + count, key); // TODO 配合测试用例, 测完要删掉
		int newsize = newlist.size();
		//将过滤出的条数存储到操作日志
        if (billLoggerEntity != null){
        	billLoggerEntity.setFiltersNumber(newsize);
        }
		if (0 != newsize){
        	log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Found {} letter mail new suspected bill.", new Object[]{mailUrl, accountId, phoneId, newsize});
        	this.send("A1`发现有："+ newsize +"封新疑似邮件！", key);
			CountDownLatch downLatch = new CountDownLatch(newsize);
			// 解析所有邮件
			for(int j = 0; j < newsize; j++){
				mailId = newlist.get(j);
				lr = hc.get("http://set2.mail.qq.com/cgi-bin/readmail?folderid=1&folderkey=1&t=readmail&mailid="+mailId+"&mode=pre&maxage=3600&base=12.52&ver=10920&sid="+sid+"#stattime=1405496987884",setHeaderToGetMailDoc(sid));
				Document docMail = Jsoup.parse(lr.getHtml("gb18030"));
				
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Start processing the {} mail.", new Object[]{mailUrl, accountId, phoneId, j+1});
				this.send("B1`正在解析第：" + (j+1) + "/" + size +"封邮件", key);
				StringBuffer content = new StringBuffer();
				content.append(docMail.toString());
				if(docMail.select("span[id=subject]").first() != null){
					subject = docMail.select("span[id=subject]").first().html();
				}
				if(docMail.select("b[id=tipFromAddr_readmail]").first() != null){
					senderAdd = docMail.select("b[id=tipFromAddr_readmail]").first().attr("fromaddr");
				}
				if(docMail.select("div[class=addrtitle nowrap]").first() != null){
					receiveAdd = docMail.select("div[class=addrtitle nowrap]").first().nextElementSibling().childNode(0).attr("e");
				}
				if(docMail.select("td[class=txt_right settingtable noUnderLineList]").first() != null){
					sentData = docMail.select("td[class=txt_right settingtable noUnderLineList]").first().previousElementSibling().child(1).html();
					//日期格式转化（yyyy-MM-dd E HH:mm:ss）
					year = sentData.substring(0,sentData.indexOf("年"));
					month = sentData.substring(sentData.indexOf("年")+1,sentData.indexOf("月"));
					day = sentData.substring(sentData.indexOf("月")+1,sentData.indexOf("日"));
					week = sentData.substring(sentData.indexOf("(")+1,sentData.indexOf(")"));
					timeSlot = sentData.substring(sentData.indexOf(")")+2,sentData.indexOf(")")+4);
					hour = sentData.substring(sentData.indexOf(")")+4,sentData.indexOf(":"));
					min = sentData.substring(sentData.indexOf(":")+1,sentData.length());
					hourInt = Integer.parseInt(hour);
					if(timeSlot.equals("中午") && hourInt == 1 || timeSlot.equals("下午") || timeSlot.equals("晚上")){
						hourInt+=12;
						hour = String.valueOf(hourInt);
					}
					sentData = year+"-"+month+"-"+day+" "+week+" "+hour+":"+min;
				}
				log.info("e_mail:{} \taccount_id:{} \tphone_id:{} \ttheme: {} \tthe_sender: {} \taddressee：{} \tsend_time：{}.", 
						new Object[]{mailUrl, accountId, phoneId, subject, senderAdd, receiveAdd, sentData});
				taskExecutor.execute(new ThreadRunAbleTest(parseMailService, downLatch, subject, receiveAdd, content, sentData, senderAdd, mailUrl, count, key, accountid_phoneId, logMap, newsize, scVersion));
			}
			try {
				downLatch.await();
			} catch (InterruptedException e) {
				MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INTERRUPTED_EXCEPTION_CODE, ErrorCodeContants.INTERRUPTED_EXCEPTION.getMsg(), log);
			}
		}else{
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Messages not found will be parsing!",  new Object[]{mailUrl, accountId, phoneId});
			this.send("A1`没发现新疑似邮件！", key);
		}
		// 退出登录
		lr = hc.get("http://mail.qq.com/cgi-bin/logout?sid="+sid+"&loc=frame_html,,,5");	
    }
    
    //获取登录验证码
    
	@SuppressWarnings("static-access")
	private String[] getVcode(HttpResult lr,HttpClientHelper hc,String mailUrl,String key){
    	log.info("e_mail:{} the user name for {} users require the user to manually enter the verification code!",new Object[]{mailUrl, mailUrl});
    	String uin = mailUrl.substring(0,mailUrl.indexOf("@"));
    	lr = vCodeImgUrl(lr, hc, uin, key);
    	String code = super.vcodeMap.get(key);
    	while(code == null){
    		code = super.vcodeMap.get(key);
    	}
    	String loginInfo = code.substring(code.indexOf("Client:RD1`")+11, code.length());
    	String[] loginInfoArray = loginInfo.split("\\{umP\\}");
		return loginInfoArray;
    }
    
    //登录
	
	@SuppressWarnings("static-access")
	private HttpResult login(HttpResult lr,HttpClientHelper hc,String[] info,String key) throws MailBillException{
    	String mailUrl = info[0];
    	String password = info[1];
    	String v_code = info[2];
    	// 解析腾讯JS加密
        ScriptEngineManager mgr = new ScriptEngineManager(); 
        ScriptEngine engine = mgr.getEngineByExtension("js");
        //截取用户的qq号
    	String mailqq = mailUrl.substring(0, mailUrl.indexOf("@"));
    	//获取加密后的password
		String passwordMd5 = javascript_Password(engine,mailqq,password,v_code);
		//执行登录
		lr = hc.get("https://ssl.ptlogin2.qq.com/login?u="+mailqq+"%40qq.com&verifycode="+v_code+"&p="+passwordMd5+"&pt_rsa=0&u1=https%3A%2F%2Fmail.qq.com%2Fcgi-bin%2Flogin%3Fvt%3Dpassport%26vm%3Dwpt%26ft%3Dloginpage%26target%3D%26account%3D"+mailqq+"%2540qq.com&ptredirect=1&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=2-9-1402296239152&js_ver=10080&js_type=1&login_sig=&pt_uistyle=25&aid=522005705&daid=4&",setHeaderToLogin());
		this.vcodeMap.remove(key);
		return lr;
    }
	
	//判断用户是否需要独立密码
	
	@SuppressWarnings("static-access")
	private HttpResult ipwdJudge(HttpResult lr,HttpClientHelper hc,Map<String,String> data,String mailUrl,String new_url,String key){
		if(lr.getHtml("gb18030").contains("验证独立密码")){
			this.send("A1`您设置了邮箱独立密码，请输入...",key);
			log.info("e_mail:{} please enter your QQ mailbox independent password",mailUrl);
			lr = iPassword(lr,hc,data,key);
			//满足条件则独立密码输入有误
			while(lr.getHtml("gb18030").contains("&msg=wrong")){
				this.send("A1`独立密码输入有误，请重新输入...",key);
				log.error("e_mail:{} login failed, please check the information you entered", mailUrl);
				lr = hc.get(new_url,setHeaderToGetSid());
				lr = iPassword(lr,hc,data,key);
			}
			//满足条件则需要输入独立密码的验证码
			while(lr.getHtml("gb18030").contains("verify=true") || lr.getHtml("gb18030").contains("&msg=wrong")){
				this.send("A1`请输入验证码...",key);
				//获取验证码 获得用户名、独立密码、验证码
				vCodeImgUrlIpwd(lr, hc, key);
		    	String code = super.vcodeMap.get(key);
		    	while(code == null){
		    		code = super.vcodeMap.get(key);
		    	}
		    	//刷新独立密码验证码
		    	String[] loginInfoArray = new String[3];
		    	if(code.contains("refreshIpwd")){
		    		loginInfoArray = refreshIpwd(lr, hc, loginInfoArray, key);
		    	}else{
		    		String loginInfo = code.substring(code.indexOf("Client:RD1`")+11, code.length());
		    		loginInfoArray = loginInfo.split("\\{umP\\}");
		    	}
				//用户名处理
				String clientaddr =  loginInfoArray[0].substring(0,loginInfoArray[0].indexOf("@"));
				//重新提取表单中的post信息
				lr = hc.get(new_url,setHeaderToGetSid());
	 	        Document doc2 = Jsoup.parse(lr.getHtml("gb18030"));
				String org_errtype2 = doc2.select("input[name=org_errtype]").first().attr("value");
				String tfcont2 = doc2.select("input[name=tfcont]").first().attr("value");
				String delegate_url2 = doc2.select("input[name=delegate_url]").first().attr("value");
				String f2 = doc2.select("input[name=f]").first().attr("value");
				String starttime2 = doc2.select("input[name=starttime]").first().attr("value");
				String chg2 = doc2.select("input[name=chg]").first().attr("value");
				String ept2 = doc2.select("input[name=ept]").first().attr("value");
				String ppp2 = doc2.select("input[name=ppp]").first().attr("value");
				String ts2 = doc2.select("input[name=ts]").first().attr("value");
				String vt2 = doc2.select("input[name=vt]").first().attr("value");
				String spcache2 = doc2.select("input[name=spcache]").first().attr("value");
				
				data.clear();
				data.put("org_errtype", org_errtype2);
				data.put("tfcont", tfcont2);
				data.put("delegate_url", delegate_url2);
				data.put("f", f2);
				data.put("starttime", starttime2);
				data.put("chg", chg2);
				data.put("ept", ept2);
				data.put("ppp", ppp2);
				data.put("ts", ts2);
				data.put("vt", vt2);
				data.put("spcache", spcache2);
				data.put("clientaddr", clientaddr);
				data.put("pp", loginInfoArray[1]);
				data.put("p", loginInfoArray[1]);
				data.put("verifycode", loginInfoArray[2]);
				data.put("btlogin", "");
				lr = hc.post("https://mail.qq.com/cgi-bin/login?sid=,2,zh_CN",data,setHeaderToIpasswordLogin());
				this.vcodeMap.remove(key);
			}
		}
		return lr;
	}
	
	//登录验证码请求链接
    private HttpResult vCodeImgUrl(HttpResult lr, HttpClientHelper hc, String uin, String key){
    	lr = hc.get("https://ssl.captcha.qq.com/getimage?uin="+uin, setHeaderGetLoginVcode());
    	this.send("D1`登录验证码url为："+"data:image/jpg;base64,"+Base64.encode(lr.getResponse()),key);
		return lr;
    }
    
    //独立密码验证码请求链接
    private void vCodeImgUrlIpwd(HttpResult lr, HttpClientHelper hc, String key){
    	lr = hc.get("https://mail.qq.com/cgi-bin/getverifyimage?aid=23000101&f=html&ck=1&r=0.6234695543535054", setHeaderToGetIpwdimg());
		this.send("D1`独立密码验证码url为："+"data:image/jpg;base64,"+Base64.encode(lr.getResponse()),key);
    }
    
    //验证码刷新
    
	@SuppressWarnings("static-access")
	private String[] refresh(HttpResult lr, HttpClientHelper hc, String[] info, String uin, String key){
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
        		vCodeImgUrl(lr, hc, uin, key);
        		this.vcodeMap.remove(key);
        	}
    	}
		return info;
    }
    
    //刷新独立密码验证码
    
	@SuppressWarnings("static-access")
	private String[] refreshIpwd(HttpResult lr, HttpClientHelper hc, String[] info, String key){
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
        	if(code.contains("refreshIpwd")){
        		vCodeImgUrlIpwd(lr, hc, key);
        		this.vcodeMap.remove(key);
        	}
    	}
		return info;
    }
    
    //独立密码输入
    
	@SuppressWarnings("static-access")
	private HttpResult iPassword(HttpResult lr,HttpClientHelper hc,Map<String,String> data,String key){
		this.send("H1`需要独立密码",key);
		String code = super.vcodeMap.get(key);
    	while(code == null){
    		code = super.vcodeMap.get(key);
    	}
    	String loginInfo = code.substring(code.indexOf("Client:RH1`")+11, code.length());
    	String[] loginInfoArray = loginInfo.split("\\{umP\\}");
		String clientaddr = loginInfoArray[0].substring(0,loginInfoArray[0].indexOf("@"));
		
		//从页面中提取表单数据
		Document doc = Jsoup.parse(lr.getHtml("gb18030"));
		String org_errtype = doc.select("input[name=org_errtype]").first().attr("value");
		String tfcont = doc.select("input[name=tfcont]").first().attr("value");
		String delegate_url = doc.select("input[name=delegate_url]").first().attr("value");
		String f = doc.select("input[name=f]").first().attr("value");
		String starttime = doc.select("input[name=starttime]").first().attr("value");
		String chg = doc.select("input[name=chg]").first().attr("value");
		String ept = doc.select("input[name=ept]").first().attr("value");
		String ppp = doc.select("input[name=ppp]").first().attr("value");
		String ts = doc.select("input[name=ts]").first().attr("value");
		String vt = doc.select("input[name=vt]").first().attr("value");
		String spcache = doc.select("input[name=spcache]").first().attr("value");
		String verifycode = doc.select("input[name=verifycode]").first().attr("value");
		
		data.clear();
		data.put("org_errtype", org_errtype);
		data.put("tfcont", tfcont);
		data.put("delegate_url", delegate_url);
		data.put("f", f);
		data.put("starttime", starttime);
		data.put("chg", chg);
		data.put("ept", ept);
		data.put("ppp", ppp);
		data.put("ts", ts);
		data.put("vt", vt);
		data.put("spcache", spcache);
		data.put("clientaddr", clientaddr);
		data.put("pp", loginInfoArray[1]);
		data.put("p", loginInfoArray[1]);
		data.put("verifycode", verifycode);
		data.put("btlogin", "");
		lr = hc.post("https://mail.qq.com/cgi-bin/login?sid=,2,zh_CN",data,setHeaderToIpasswordLogin());
		this.vcodeMap.remove(key);
		return lr;
    }
    
    // 递归过滤
    
	private int reScan(HttpResult lr,HttpClientHelper hc,List<String> mailIdList, String mailId, String sid, String subject, String senderAdd, 
    		String receiveAdd, String sentData, String username, int count, String key, String accountid_phoneId, Map<String, BillLogEntity> logMap,
    		Map<String, String> map, List<String> list) throws MailBillException{
    	List<String> mailIdScan = new ArrayList<String>();
    	
    	//分解accountId与phoneId
        String[] split = accountid_phoneId.split("_");
		String accountId = null;
		String phoneId = null;
		if (split.length != 0){
			accountId = split[0];
			phoneId = split[1];
		}
    	
		log.info("e_mail:{} \taccount_id:{} \tphone_id:{} there are {} mail need to filter, began to recursive filtering", new Object[]{username, accountId, phoneId, mailIdList.size()});
		for(int i = 0; i < mailIdList.size(); i++){
			try {
				// 依次读取邮件时 需休眠300毫秒
				Thread.sleep(Constants.QQ_MAIL_FREQUENCY_SECONDS);
			} catch (InterruptedException e) {
				throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INTERRUPTED_EXCEPTION_CODE, ErrorCodeContants.INTERRUPTED_EXCEPTION.getMsg(), log);
			}	
			mailId = mailIdList.get(i);
			lr = hc.get("http://set2.mail.qq.com/cgi-bin/readmail?folderid=1&folderkey=1&t=readmail&mailid="+mailId+"&mode=pre&maxage=3600&base=12.52&ver=10920&sid="+sid+"#stattime=1405496987884",setHeaderToGetMailDoc(sid));
			Document docMail = Jsoup.parse(lr.getHtml("gb18030"));
			if(lr.getHtml("gb18030").contains("您请求的频率太快，请稍后再试")){
				mailIdScan.add(mailId);
				continue;
			}
			if(lr.getHtml("gb18030").equals("")){
				continue;
			}
			StringBuffer content = new StringBuffer();
			content.append(docMail.toString());
			if(docMail.select("span[id=subject]").first() != null){
				subject = docMail.select("span[id=subject]").first().html();
			}
			if(docMail.select("b[id=tipFromAddr_readmail]").first() != null){
				senderAdd = docMail.select("b[id=tipFromAddr_readmail]").first().attr("fromaddr");
			}
			if(docMail.select("div[class=addrtitle nowrap]").first() != null){
				receiveAdd = docMail.select("div[class=addrtitle nowrap]").first().nextElementSibling().childNode(0).attr("e");
			}
			if(docMail.select("td[class=txt_right settingtable noUnderLineList]").first() != null){
				sentData = docMail.select("td[class=txt_right settingtable noUnderLineList]").first().previousElementSibling().child(1).html();
			}
			log.info("e_mail:{} \taccount_id:{} \tphone_id:{} Querying the {} e-mail.",new Object[]{username, accountId, phoneId, ++count});
			this.send("A1`正在查询第：" + count +"封邮件", key);
			String[] path = mailAnalyze.getPath(subject, receiveAdd, sentData, senderAdd, username);
			/*if (!StringUtils.isBlank(map.get(path[0]))){
				map.remove(path[0]);
			}else{
				list.add(mailId);
			}*/
			if (StringUtils.isBlank(map.get(path[0]))){ // 库中无此记录,是为新发现的疑似账单
				list.add(mailId);
			}
		}
		
		if(mailIdScan.size() != 0){	//递归出口 为0则跳出
			count = reScan(lr,hc,mailIdScan,mailId,sid,subject,senderAdd,receiveAdd,sentData,username,count, key, accountid_phoneId, logMap, map, list);
		}
		
		return count;
    }
    
    // -------------- private function -------------
    // 获取登录时加密后的password
    
	private String javascript_Password(ScriptEngine engine, String username_sub, String password, String v_code) throws MailBillException {   
        String script =  "var hexcase = 1;"
					    +"var b64pad = \"\";"
					    +"v" +
					    "ar chrsz = 8;"
					    +"var mode = 32;"
					    +"function md5(s) {"
					    +"    return hex_md5(s)"
					    +"}"
					    +"function hex_md5(s) {"
					    +"    return binl2hex(core_md5(str2binl(s), s.length * chrsz))"
					    +"}"
					    +"function str2binl(str) {"
					    +"    var bin = Array();"
					    +"    var mask = (1 << chrsz) - 1;"
					    +"    for (var i = 0; i < str.length * chrsz; i += chrsz) {"
					    +"        bin[i >> 5] |= (str.charCodeAt(i / chrsz) & mask) << (i % 32)"
					    +"    }"
					    +"    return bin"
					    +"}"
					    +"function core_md5(x, len) {"
					    +"    x[len >> 5] |= 128 << ((len) % 32);"
					    +"    x[(((len + 64) >>> 9) << 4) + 14] = len;"
					    +"    var a = 1732584193;"
					    +"    var b = -271733879;"
					    +"    var c = -1732584194;"
					    +"    var d = 271733878;"
					    +"    for (var i = 0; i < x.length; i += 16) {"
					    +"        var olda = a;"
					    +"        var oldb = b;"
					    +"        var oldc = c;"
					    +"        var oldd = d;"
					    +"        a = md5_ff(a, b, c, d, x[i + 0], 7, -680876936);"
					    +"        d = md5_ff(d, a, b, c, x[i + 1], 12, -389564586);"
					    +"        c = md5_ff(c, d, a, b, x[i + 2], 17, 606105819);"
					    +"        b = md5_ff(b, c, d, a, x[i + 3], 22, -1044525330);"
					    +"        a = md5_ff(a, b, c, d, x[i + 4], 7, -176418897);"
					    +"        d = md5_ff(d, a, b, c, x[i + 5], 12, 1200080426);"
					    +"        c = md5_ff(c, d, a, b, x[i + 6], 17, -1473231341);"
					    +"        b = md5_ff(b, c, d, a, x[i + 7], 22, -45705983);"
					    +"        a = md5_ff(a, b, c, d, x[i + 8], 7, 1770035416);"
					    +"        d = md5_ff(d, a, b, c, x[i + 9], 12, -1958414417);"
					    +"        c = md5_ff(c, d, a, b, x[i + 10], 17, -42063);"
					    +"        b = md5_ff(b, c, d, a, x[i + 11], 22, -1990404162);"
					    +"        a = md5_ff(a, b, c, d, x[i + 12], 7, 1804603682);"
					    +"        d = md5_ff(d, a, b, c, x[i + 13], 12, -40341101);"
					    +"        c = md5_ff(c, d, a, b, x[i + 14], 17, -1502002290);"
					    +"        b = md5_ff(b, c, d, a, x[i + 15], 22, 1236535329);"
					    +"        a = md5_gg(a, b, c, d, x[i + 1], 5, -165796510);"
					    +"        d = md5_gg(d, a, b, c, x[i + 6], 9, -1069501632);"
					    +"        c = md5_gg(c, d, a, b, x[i + 11], 14, 643717713);"
					    +"        b = md5_gg(b, c, d, a, x[i + 0], 20, -373897302);"
					    +"        a = md5_gg(a, b, c, d, x[i + 5], 5, -701558691);"
					    +"        d = md5_gg(d, a, b, c, x[i + 10], 9, 38016083);"
					    +"        c = md5_gg(c, d, a, b, x[i + 15], 14, -660478335);"
					    +"        b = md5_gg(b, c, d, a, x[i + 4], 20, -405537848);"
					    +"        a = md5_gg(a, b, c, d, x[i + 9], 5, 568446438);"
					    +"        d = md5_gg(d, a, b, c, x[i + 14], 9, -1019803690);"
					    +"        c = md5_gg(c, d, a, b, x[i + 3], 14, -187363961);"
					    +"        b = md5_gg(b, c, d, a, x[i + 8], 20, 1163531501);"
					    +"        a = md5_gg(a, b, c, d, x[i + 13], 5, -1444681467);"
					    +"        d = md5_gg(d, a, b, c, x[i + 2], 9, -51403784);"
					    +"        c = md5_gg(c, d, a, b, x[i + 7], 14, 1735328473);"
					    +"        b = md5_gg(b, c, d, a, x[i + 12], 20, -1926607734);"
					    +"        a = md5_hh(a, b, c, d, x[i + 5], 4, -378558);"
					    +"        d = md5_hh(d, a, b, c, x[i + 8], 11, -2022574463);"
					    +"        c = md5_hh(c, d, a, b, x[i + 11], 16, 1839030562);"
					    +"        b = md5_hh(b, c, d, a, x[i + 14], 23, -35309556);"
					    +"        a = md5_hh(a, b, c, d, x[i + 1], 4, -1530992060);"
					    +"        d = md5_hh(d, a, b, c, x[i + 4], 11, 1272893353);"
					    +"        c = md5_hh(c, d, a, b, x[i + 7], 16, -155497632);"
					    +"        b = md5_hh(b, c, d, a, x[i + 10], 23, -1094730640);"
					    +"        a = md5_hh(a, b, c, d, x[i + 13], 4, 681279174);"
					    +"        d = md5_hh(d, a, b, c, x[i + 0], 11, -358537222);"
					    +"        c = md5_hh(c, d, a, b, x[i + 3], 16, -722521979);"
					    +"        b = md5_hh(b, c, d, a, x[i + 6], 23, 76029189);"
					    +"        a = md5_hh(a, b, c, d, x[i + 9], 4, -640364487);"
					    +"        d = md5_hh(d, a, b, c, x[i + 12], 11, -421815835);"
					    +"        c = md5_hh(c, d, a, b, x[i + 15], 16, 530742520);"
					    +"        b = md5_hh(b, c, d, a, x[i + 2], 23, -995338651);"
					    +"        a = md5_ii(a, b, c, d, x[i + 0], 6, -198630844);"
					    +"        d = md5_ii(d, a, b, c, x[i + 7], 10, 1126891415);"
					    +"        c = md5_ii(c, d, a, b, x[i + 14], 15, -1416354905);"
					    +"        b = md5_ii(b, c, d, a, x[i + 5], 21, -57434055);"
					    +"        a = md5_ii(a, b, c, d, x[i + 12], 6, 1700485571);"
					    +"        d = md5_ii(d, a, b, c, x[i + 3], 10, -1894986606);"
					    +"        c = md5_ii(c, d, a, b, x[i + 10], 15, -1051523);"
					    +"        b = md5_ii(b, c, d, a, x[i + 1], 21, -2054922799);"
					    +"        a = md5_ii(a, b, c, d, x[i + 8], 6, 1873313359);"
					    +"        d = md5_ii(d, a, b, c, x[i + 15], 10, -30611744);"
					    +"        c = md5_ii(c, d, a, b, x[i + 6], 15, -1560198380);"
					    +"        b = md5_ii(b, c, d, a, x[i + 13], 21, 1309151649);"
					    +"        a = md5_ii(a, b, c, d, x[i + 4], 6, -145523070);"
					    +"        d = md5_ii(d, a, b, c, x[i + 11], 10, -1120210379);"
					    +"        c = md5_ii(c, d, a, b, x[i + 2], 15, 718787259);"
					    +"        b = md5_ii(b, c, d, a, x[i + 9], 21, -343485551);"
					    +"        a = safe_add(a, olda);"
					    +"        b = safe_add(b, oldb);"
					    +"        c = safe_add(c, oldc);"
					    +"        d = safe_add(d, oldd)"
					    +"    }"
					    +"    if (mode == 16) {"
					    +"        return Array(b, c)"
					    +"    } else {"
					    +"        return Array(a, b, c, d)"
					    +"    }"
					    +"}"
					    +"function md5_cmn(q, a, b, x, s, t) {"
					    +"    return safe_add(bit_rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b)"
					    +" }"
					    +"function bit_rol(num, cnt) {"
					    +"    return (num << cnt) | (num >>> (32 - cnt))"
					    +"}"
					    +"function md5_ff(a, b, c, d, x, s, t) {"
					    +"    return md5_cmn((b & c) | ((~b) & d), a, b, x, s, t)"
					    +"}"
					    +"function md5_gg(a, b, c, d, x, s, t) {"
					    +"    return md5_cmn((b & d) | (c & (~d)), a, b, x, s, t)"
					    +"}"
					    +"function md5_hh(a, b, c, d, x, s, t) {"
					    +"    return md5_cmn(b ^ c ^ d, a, b, x, s, t)"
					    +"}"
					    +"function md5_ii(a, b, c, d, x, s, t) {"
					    +"    return md5_cmn(c ^ (b | (~d)), a, b, x, s, t)"
					    +"}"
					    +"function safe_add(x, y) {"
					    +"    var lsw = (x & 65535) + (y & 65535);"
					    +"    var msw = (x >> 16) + (y >> 16) + (lsw >> 16);"
					    +"    return (msw << 16) | (lsw & 65535)"
					    +"}"
					    +"function binl2hex(binarray) {"
					    +"    var hex_tab = hexcase ? \"0123456789ABCDEF\": \"0123456789abcdef\";"
					    +"    var str = \"\";"
					    +"    for (var i = 0; i < binarray.length * 4; i++) {"
					    +"        str += hex_tab.charAt((binarray[i >> 2] >> ((i % 4) * 8 + 4)) & 15) + hex_tab.charAt((binarray[i >> 2] >> ((i % 4) * 8)) & 15)"
					    +"    }"
					    +"    return str"
					    +"}"
					    +"function hexchar2bin(str) {"
					    +"    var arr = [];"
					    +"    for (var i = 0; i < str.length; i = i + 2) {"
					    +"        arr.push(\"\\\\x\" + str.substr(i, 2))"
					    +"    }"
					    +"    arr = arr.join(\"\");"
					    +"    eval(\"var temp = '\" + arr + \"'\");"
					    +"    return temp"
					    +"}"
					    +"function getEncryption(password, uin, vcode) {"
					    +"    var str1 = hexchar2bin(md5(password));"
					    +"    var str2 = md5(str1 + uin);"
					    +"    var str3 = md5(str2 + vcode.toUpperCase());"
					    +"    return str3"
					    +"}"
					    +"function uin2hex(str) {"
					    +"    var maxLength = 16;"
				        +"    str = parseInt(str);"
				        +"    var hex = str.toString(16);"
				        +"    var len = hex.length;"
				        +"    for (var i = len; i < maxLength; i++) {"
				        +"        hex = \"0\" + hex"
				        +"    }"
				        +"    var arr = [];"
				        +"    for (var j = 0; j < maxLength; j += 2) {"
				        +"        arr.push(\"\\\\x\" + hex.substr(j, 2))"
				        +"    }"
				        +"    var result = arr.join(\"\");"
				        +"    eval('result=\"' + result + '\"');"
				        +"    return result"
				        +"}";
        try {
			engine.eval(script);
	        Invocable inv = (Invocable) engine;
	        String uin = (String) inv.invokeFunction("uin2hex", username_sub);
	        String res = (String) inv.invokeFunction("getEncryption", password,uin,v_code);   
	        log.info("res:" + res);//加密后的密码 参数p的valve
	        return res;
        } catch (ScriptException e) {
        	throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.SCRIPT_EXCEPTION_CODE, ErrorCodeContants.SCRIPT_EXCEPTION.getMsg(), log);
        } catch (NoSuchMethodException e) {
        	throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.NOSUCH_METHOD_EXCEPTION_CODE, ErrorCodeContants.NOSUCH_METHOD_EXCEPTION.getMsg(), log);
		}
    }
    
    // 设置登录时url的头信息
    private Header[] setHeaderToLogin() {
        Header[] result = {
        		new BasicHeader("Host","ssl.ptlogin2.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
        		new BasicHeader("Accept","*/*"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=522005705&daid=4&s_url=https://mail.qq.com/cgi-bin/login?vt=passport%26vm=wpt%26ft=loginpage%26target=&style=25&low_login=1&proxy_url=https://mail.qq.com/proxy.html&need_qr=0&hide_border=1&border_radius=0&self_regurl=http://zc.qq.com/chs/index.html?type=1&app_id=11005?t=regist&pt_feedback_link=http://support.qq.com/discuss/350_1.shtml&css=https://res.mail.qq.com/zh_CN/htmledition/style/ptlogin_input1dd8c7.css"),
                new BasicHeader("Connection","Keep-Alive")
        };
        return result;
    }
    
    // 设置获取sid时url的头信息
    private Header[] setHeaderToGetSid() {
    	Header[] result = {
        		new BasicHeader("Host","ssl.ptlogin2.mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
        		new BasicHeader("Accept","*/*"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Connection","Keep-Alive"),
                new BasicHeader("Referer","https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=522005705&daid=4&s_url=https://mail.qq.com/cgi-bin/login?vt=passport%26vm=wpt%26ft=loginpage%26target=&style=25&low_login=1&proxy_url=https://mail.qq.com/proxy.html&need_qr=0&hide_border=1&border_radius=0&self_regurl=http://zc.qq.com/chs/index.html?type=1&app_id=11005?t=regist&pt_feedback_link=http://support.qq.com/discuss/350_1.shtml&css=https://res.mail.qq.com/zh_CN/htmledition/style/ptlogin_input1e7c9d.css")
        };
        return result;
	}
    
    // 设置获取登录时验证码url的头信息
    private Header[] setHeaderToGetVcode() {
        Header[] result = {
        		new BasicHeader("Host","ssl.ptlogin2.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
        		new BasicHeader("Accept","*/*"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer","https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=522005705&daid=4&s_url=https://mail.qq.com/cgi-bin/login?vt=passport%26vm=wpt%26ft=loginpage%26target=&style=25&low_login=1&proxy_url=https://mail.qq.com/proxy.html&need_qr=0&hide_border=1&border_radius=0&self_regurl=http://zc.qq.com/chs/index.html?type=1&app_id=11005?t=regist&pt_feedback_link=http://support.qq.com/discuss/350_1.shtml&css=https://res.mail.qq.com/zh_CN/htmledition/style/ptlogin_input1dd8c7.css"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
    
    // 设置获取收信规则列表时url的头信息
    private Header[] setHeaderToGetList() {
        Header[] result = {
        		new BasicHeader("Host","mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
        		new BasicHeader("Accept"," text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Connection","keep-alive"),
        		new BasicHeader("Cache-Control","max-age=0")
        };
        return result;
    }
    
    
    
    // 设置获取mailid时url的头信息
    private Header[] setHeaderToGetMailId() {
    	Header[] result = {
        		new BasicHeader("Host","set2.mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
    
    // 设置获取mail内容时url的头信息
    private Header[] setHeaderToGetMailDoc(String sid) {
    	Header[] result = {
        		new BasicHeader("Host","set2.mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-CN,zh;q=0.8"),
                new BasicHeader("Accept-Encoding","gzip,deflate,sdch"),
                new BasicHeader("Referer","http://mail.qq.com/cgi-bin/frame_html?sid="+sid+"&r=a63d40aaf4d673a9800bb43c84ce2316"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
    
    // 设置登录时获取验证码url头信息
    private Header[] setHeaderGetLoginVcode() {
    	Header[] result = {
        		new BasicHeader("Host","setHeaderGetLoginImage"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"),
        		new BasicHeader("Accept","image/png,image/*;q=0.8,*/*;q=0.5"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Referer"," https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=522005705&daid=4&s_url=https://mail.qq.com/cgi-bin/login?vt=passport%26vm=wpt%26ft=loginpage%26target=&style=25&low_login=1&proxy_url=https://mail.qq.com/proxy.html&need_qr=0&hide_border=1&border_radius=0&self_regurl=http://zc.qq.com/chs/index.html?type=1&app_id=11005?t=regist&pt_feedback_link=http://support.qq.com/discuss/350_1.shtml&css=https://res.mail.qq.com/zh_CN/htmledition/style/ptlogin_input1f1a66.css"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
    
    //获取广告邮件中的mailid
    private Header[] setHeaderToGetAdMailId() {
    	Header[] result = {
        		new BasicHeader("Host","set2.mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3"),
                new BasicHeader("Accept-Encoding","gzip, deflate"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }

    //独立密码登录
    private Header[] setHeaderToIpasswordLogin(){
    	Header[] result = {
        		new BasicHeader("Host","mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"),
        		new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"),
        		new BasicHeader("Accept-Language","zh-CN,zh;q=0.8"),
                new BasicHeader("Accept-Encoding","gzip,deflate"),
                new BasicHeader("Content-Type","application/x-www-form-urlencoded"),
                new BasicHeader("Origin","https://mail.qq.com"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
    
    //获取独立密码验证码
    private Header[] setHeaderToGetIpwdimg(){
    	Header[] result = {
    			new BasicHeader("Host","mail.qq.com"),
        		new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"),
        		new BasicHeader("Accept","image/png,image/*;q=0.8,*/*;q=0.5"),
        		new BasicHeader("Accept-Language","zh-CN,zh;q=0.8"),
                new BasicHeader("Accept-Encoding","gzip,deflate"),
                new BasicHeader("Connection","keep-alive")
        };
        return result;
    }
}
