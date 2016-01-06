package net.umpay.mailbill.util.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 对用户邮件相关处理
 */
@Service
public class MailAnalyze {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(MailAnalyze.class);
	
	/**
	 * 邮箱的厂商类型
	 * 
	 * @param mailUrl	邮箱地址
	 * @return String	返回邮件厂商类型
	 */
	public String getHttpScanType(String mailUrl){
		String returnType = "";
		String username_sub = mailUrl.substring(mailUrl.lastIndexOf("@"));
		
		if (username_sub.contains("@qq.com")){
			returnType = MailBillTypeConstants.HTTP_SCAN_QQ_TYPE;
		}
		if (username_sub.contains("@163.com")){
			returnType = MailBillTypeConstants.HTTP_SCAN_163_TYPE;
		}
		if (username_sub.contains("@126.com")){
			returnType = MailBillTypeConstants.HTTP_SCAN_126_TYPE;
		}
		if (username_sub.contains("@yeah.net")){
			returnType = MailBillTypeConstants.HTTP_SCAN_YEAH_TYPE;
		}
		if (username_sub.contains("@139.com")){
			returnType = MailBillTypeConstants.HTTP_SCAN_139_TYPE;
		}
		
		return returnType;
	}

	/**
	 * 文件目录的获取
	 * 
	 * @param subject		邮件主题
	 * @param receiveAdd	收件人
	 * @param sentData		发送时间处理
	 * @param senderAdd		发件人mail地址
	 * @param user_mail		用户mail地址
	 * @return StringBuffer
	 */
	public String[] getPath(String subject, String receiveAdd,
			String sentData, String senderAdd, String user_mail) {
		//主题处理
		if(subject.lastIndexOf(":") != -1){
			subject = subject.substring(subject.lastIndexOf(":")+1);
		}
		//收件人处理 截取email地址
		int receiveAdd_start = receiveAdd.lastIndexOf("<")+1;
		int receiveAdd_end = receiveAdd.lastIndexOf(">");
		if(receiveAdd_start != -1 && -1 != receiveAdd_end){
			receiveAdd = receiveAdd.substring(receiveAdd_start, receiveAdd_end);
		}
		//发件人处理
		int senderAdd_start = senderAdd.lastIndexOf("<")+1;
		int senderAdd_end = senderAdd.lastIndexOf(">");
		if(senderAdd_start != -1 && -1 != senderAdd_end){
			senderAdd = senderAdd.substring(senderAdd_start, senderAdd_end);
		}
		//将发送时间 HH:mm 中的“：”替换为空格
		sentData = sentData.replaceAll(":", ",");
		
		String first_dir = user_mail.substring(0,1)+"/";	//一级目录 用户email第一个字符
		String second_dir = user_mail.substring(1,2)+"/";	//二级目录 用户email第二个字符
		String third_dir = user_mail+"/"; 					//三级目录 用户email
		String fourth_dir = senderAdd+"/";					//四级目录 发件人
		String path_dir = first_dir + second_dir + third_dir + fourth_dir; //后续dir目录
		String first_save = subject+"`"+receiveAdd+"`"+sentData+".html";	//第一次（未截取）
		String second_save = subject+"`"+receiveAdd+"`"+sentData+"_"+"second"+".html";	//第二次
		
		String[] fpath = new String[4]; 
		
		fpath[0] = path_dir + first_save;
		fpath[1] = path_dir + second_save;
		fpath[2] = path_dir;
		fpath[3] = senderAdd;
		
		return fpath;
	}
	
	/**
	 * 文件流下载mail账单
	 * 
	 * @param content		邮件内容未处理
	 * @param senderAdd		发件人mail地址
	 * @return StringBuffer
	 * @throws MailBillException 
	 */
	public String downLoadLinux(StringBuffer content,
			String senderAdd, String first_save,
			String second_save ,String path_dir) throws MailBillException {
		StringBuffer content_final = null;
		StringBuffer content_sub = new StringBuffer();
		String[] path = new String[2];
		String[] path2 = new String[2];
		/************* bill first download begin *************/
		//编码的修改
		StringBuffer string = getUtf8(content);
		//DFS文件分布式存储下载
		InputStream stringStream = new FastFDSUtils().getStringStream(string.toString());
		String[] uploadFile = new FastFDSUtils().uploadFile(Constants.DFS_GROUP, stringStream, "html", null);
		
		path[1] = Constants.DOMAIN_NAME_PATH+uploadFile[0]+"/"+uploadFile[1];
		/************* bill first download end ***************/
		
		/************* bill second download begin ***************/
		//判断是否为账单原件 即发件人为13家银行
		String bankMailAddressAll = Constants.BANK_MAIL_ADDRESS_ALL;
		//若条件成立 则说明该账单为银行原件 不做截取处理进行二次下载
		if(bankMailAddressAll.contains(senderAdd)){
			//DFS文件分布式存储下载
			path2[1] = Constants.DOMAIN_NAME_PATH+uploadFile[0]+"/"+uploadFile[1];
			content_final = MailDownload.getRemoteFile(uploadFile[0], uploadFile[1]);//将账单邮件第二次整理后的内容读取出来 进行解析
		}
		//条件成立说明邮件为转发件
		else{
			int indexOf = content.indexOf("<div");	//  目前是把原件转发 或 普通转发的邮件都从<div 开始截取
			if (-1 != indexOf) { // 转发邮件要进行截取后再保存
				content_sub.append("<head><title>" + second_save + "</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+Constants.CHARSET+"\"/></head><body>");
				content_sub.append(content.substring(indexOf)); 
				//DFS文件分布式存储下载
				InputStream stringStreamNew = new FastFDSUtils().getStringStream(content_sub.toString());
				String[] uploadFileNew = new FastFDSUtils().uploadFile(Constants.DFS_GROUP, stringStreamNew, "html", null);
				path2[1] = Constants.DOMAIN_NAME_PATH+uploadFileNew[0]+"/"+uploadFileNew[1];
				content_final = MailDownload.getRemoteFile(uploadFileNew[0], uploadFileNew[1]);//将账单邮件第二次整理后的内容读取出来 进行解析
			} else{ // 不含“<div” 是干扰内容，则原样保存
				//DFS文件分布式存储下载
				uploadFile = new FastFDSUtils().uploadFile(Constants.DFS_GROUP, stringStream, "html", null);
				path2[1] = Constants.DOMAIN_NAME_PATH+uploadFile[0]+"/"+uploadFile[1];
				content_final = MailDownload.getRemoteFile(uploadFile[0], uploadFile[1]);//将账单邮件第二次整理后的内容读取出来 进行解析
			}
			/************* bill second download end ***************/
			
		}
		return path[1]+"= ="+path2[1]+";;;"+content_final.toString();
	}
	/**
	 * 修改编码为指定的编码
	 * 
	 * @param content_forward
	 * @return
	 */
	private StringBuffer getUtf8(StringBuffer content_forward) {
		String string = content_forward.toString();
		StringBuffer buffer = new StringBuffer();
		//解决乱码问题
		if(string.contains("charset")){
			string = string.replaceFirst("(?<=charset=)[\\w-]+", Constants.CHARSET);
			return buffer.append(string);
		}else {
			StringBuffer charsetHead = new StringBuffer("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+Constants.CHARSET+"\"/></head>");
			charsetHead.append(content_forward);
			// content_forward.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+Constants.CHARSET+"\"/></head>");
			//return content_forward;
			return charsetHead;
		}
	}

	/**
	 * 获得邮件主题
	 * 
	 * @param msg	邮件内容
	 * @return 		解码后的邮件主题
	 * @throws MailBillException 
	 */
	
	public String getSubject(MimeMessage msg) {
		try {
			return MimeUtility.decodeText(msg.getSubject());
		} catch (UnsupportedEncodingException e) {
			MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION_CODE, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION.getMsg(), log);
		} catch (MessagingException e) {
			MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.GETSUBJECT_FAILED_CODE, ErrorCodeContants.GETSUBJECT_FAILED.getMsg(), log);
		}
		return null;
	}
	 
	/**
	 * 获得邮件发件人
	 * 
	 * @param msg	邮件内容
	 * @return 		姓名 <Email地址>
	 * @throws MailBillException 
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	
	public String getFrom(MimeMessage msg) throws MailBillException {
		String from = "";
		Address[] froms;
		try {
			froms = msg.getFrom();
			if (froms.length < 1)
				throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.NO_SENDER_CODE, "没有发件人!", log);
			InternetAddress address = (InternetAddress) froms[0];
			String person = address.getPersonal();
				if (person != null) {
					person = MimeUtility.decodeText(person) + " ";
				} else {
					person = "";
				}
			from = person + "<" + address.getAddress() + ">";
		} catch (MessagingException e1) {
			MailBillExceptionUtil.getWithLog(e1, ErrorCodeContants.GETSENDER_FAILED_EXCEPTION_CODE, ErrorCodeContants.GETSENDER_FAILED_EXCEPTION.getMsg(), log);
		} catch (UnsupportedEncodingException e) {
			MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION_CODE, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION.getMsg(), log);
		}

		return from;
	}

	/**
	 * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
	 * <p>
	 * Message.RecipientType.TO 收件人
	 * </p>
	 * <p>
	 * Message.RecipientType.CC 抄送
	 * </p>
	 * <p>
	 * Message.RecipientType.BCC 密送
	 * </p>
	 * 
	 * @param msg	 邮件内容
	 * @param type	收件人类型
	 * @return 		收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
	 * @throws MailBillException 
	 * @throws MessagingException
	 */
	
	public String getReceiveAddress(MimeMessage msg,
			Message.RecipientType type) throws MailBillException  {
		StringBuffer receiveAddress = new StringBuffer();
		Address[] addresss = null;
		try {
			if (type == null) {
				addresss = msg.getAllRecipients();
			} else {
				addresss = msg.getRecipients(type);
			}
	
			if (addresss == null || addresss.length < 1)
				throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.EMAIL_LOGIN_FAILED_CODE, "addresss is null", log);
			for (Address address : addresss) {
				InternetAddress internetAddress = (InternetAddress) address;
				receiveAddress.append(internetAddress.toUnicodeString())
						.append(",");
			}
	
			receiveAddress.deleteCharAt(receiveAddress.length() - 1); // 删除最后一个逗号
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.GETEMAIL_ADDRESSS_FAILED_CODE, ErrorCodeContants.GETEMAIL_ADDRESSS_FAILED.getMsg(), log);
		}
		return receiveAddress.toString();
	}

	/**
	 * 获得邮件发送时间
	 * 
	 * @param msg	邮件内容
	 * @return 		yyyy年mm月dd日 星期X HH:mm
	 * @throws MailBillException 
	 * @throws MessagingException
	 */
	
	public String getSentDate(MimeMessage msg, String pattern) throws MailBillException{
		try {
			Date receivedDate;
				receivedDate = msg.getSentDate();
			if (receivedDate == null)
				return "";
	
			if (pattern == null || "".equals(pattern))
				pattern = "yyyy年MM月dd日 E HH:mm";
	
			return new SimpleDateFormat(pattern).format(receivedDate);
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(ErrorCodeContants.RECEIVEDDATE_ISNULL_CODE, ErrorCodeContants.RECEIVEDDATE_ISNULL.getMsg(), log);
		}
	}

	/**
	 * 获得邮件文本内容
	 * 
	 * @param part		邮件体
	 * @param content	存储邮件文本内容的字符串
	 * @throws MailBillException 
	 * @throws MessagingException
	 * @throws IOException
	 */
	
	public void getMailTextContent(Part part, StringBuffer content) throws MailBillException{
		// 如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
		boolean isContainTextAttach;
		try {
			isContainTextAttach = part.getContentType().indexOf("name") > 0;
			if (part.isMimeType("text/*") && !isContainTextAttach) {
				content.append(part.getContent().toString());
			} else if (part.isMimeType("message/rfc822")) {
				getMailTextContent((Part) part.getContent(), content);
			} else if (part.isMimeType("multipart/*")) {
				Multipart multipart = (Multipart) part.getContent();
				int partCount = multipart.getCount();
				for (int i = 0; i < partCount; i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					getMailTextContent(bodyPart, content);
				}
			}
		} catch (MessagingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.GETCONTENT_FAILED_CODE, ErrorCodeContants.GETCONTENT_FAILED.getMsg(), log);
		} catch (IOException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.GETCONTENT_FAILED_CODE, ErrorCodeContants.GETCONTENT_FAILED.getMsg(), log);
		}
	}

}
