package net.umpay.mailbill.util.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定义异常码信息类
 * 
 * @author zhaozj
 * @version 1.0.0
 */
public class ErrorCodeContants {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory
			.getLogger(ErrorCodeContants.class);

	// ---------- error_code -------begin--------
	public static final int SUCCESS_CODE = 200;

	public static final int PASSWORD_INCORRECT_CODE = -201; // 网络连接异常
	public static final int EMAIL_LOGIN_FAILED_CODE = -202; // 邮箱登录失败,邮箱或密码错误
	public static final int SYSTEM_EXCEPTION_CODE = -203; // 系统异常
	public static final int UNKNOWN_ERROR_CODE = -204; // 未知的错误
	public static final int POP_NOT_OPEN_EXCEPTION_CODE = -205; // pop协议未开启
	public static final int IMAP_NOT_OPEN_EXCEPTION_CODE = -206; // imap协议未开启
	public static final int HTTP_SSL_EXCEPTION_CODE = -207; // ssl异常

	public static final int NOT_SUPPORT_BANK_TYPE_CODE = 201; // 不支持该银行模板类型
	public static final int DECODE_EXCEPTION_CODE = 202; // 解码信息异常
	public static final int ENCODE_EXCEPTION_CODE = 203; // 编码信息异常
	public static final int NOT_FOUND_MAIL_HANDLER_CODE = 204; // 未找到邮件处理类
	public static final int BUSINESS_PROCESS_ERROR_CODE = 205; // 业务流程错误
	public static final int CAN_NOT_INVOKE_CODE = 206; // 数据类型不支持该方法
	public static final int VALIDATE_ERROR_CODE = 207; // 验证错误
	public static final int ANALYSIS_DATA_EXCEPTION_CODE = 208; // 数据解析异常
	public static final int MAIL_NOT_FOUND_CODE = 209; // 邮箱地址未找到
	public static final int DATA_TYPE_CONVERSION_CODE = 210; // 数据类型转换异常
	public static final int NOT_SUPPORT_EMAIL_CODE = 211; // 暂不支持该邮箱
	public static final int MAIL_FORMAT_EXCEPTION_CODE = 212; // 邮箱地址非法
	public static final int EMAIL_INVALID_PROTOCOL_CODE = 213; // 邮箱协议无效
	public static final int IO_EXCEPTION_CODE = 214; // IO异常
	
	public static final int ADDRESSS_ISNULL_CODE = 215; // 收件人邮件地址为空异常
	public static final int RECEIVEDDATE_ISNULL_CODE = 216; // 发送邮件的时间为空异常
	public static final int NULL_CODE = 217; // 为空异常
	public static final int ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION_CODE = 218; // 数组越界异常
	public static final int JSON_FORMAT_EXCEPTION_CODE = 219; // json格式异常
	public static final int INVALID_KEYS_EXCEPTION_CODE = 220; // 秘钥无效
	public static final int CIPHER_PADDING_CODE = 221; // cipher的获取实例异常
	public static final int ENCRYPTION_FAILED_EXCEPTION_CODE = 222; // cipher的获取实例后加密失败异常
	public static final int UNSUPPORTED_ENCODING_EXCEPTION_CODE = 223; // 不支持的字符编码。
	public static final int GETCONTENT_FAILED_CODE = 224; // 获取邮件内容失败异常
	public static final int PASSWORD_SIZE_EXCEPTION_CODE = 225; // 密码块加密异常
	public static final int PASSWORD_ENCRYPTION_EXCEPTION_CODE = 226; // 密码解密异常
	public static final int GETSUBJECT_FAILED_CODE = 227; // 获取主题失败异常
	public static final int GETSENDER_FAILED_EXCEPTION_CODE = 228; // 获取主题失败异常
	public static final int NO_SENDER_CODE = 229; // 没有发件人异常
	public static final int GETEMAIL_ADDRESSS_FAILED_CODE = 230; // 获取邮件收件人、抄送和密送地址
	public static final int NOT_FOUND_PARSED_MAIL_CODE = 231; // 未找到要解析的邮件!
	public static final int SEARCH_FAILED_CODE = 232; //过滤邮件异常
	public static final int GET_EMAIL_FOLDER_FAILED_CODE = 233; //获取邮件目录异常
	public static final int SQL_UPDATE_FAILED_CODE = 234; //数据更新失败
	public static final int DFS_UNKNOWN_ERROR_CODE = 235; //DFS未知异常
	public static final int File_Not_Found_EXCEPTION_CODE = 236; //文件未发现异常
	public static final int NO_THIS_BILLING_TYPE_TEMPLATE_CODE = 237; //无此账单模板
	public static final int INTERRUPTED_EXCEPTION_CODE = 238; //线程中断异常
	public static final int SCRIPT_EXCEPTION_CODE = 239; //script脚本执行异常
	public static final int NOSUCH_METHOD_EXCEPTION_CODE = 240; //无此方法异常
	public static final int PASSWORD_DECRYPT3DES_EXCEPTION_CODE = 241; // 密码加密异常
	public static final int DFS_GETCONTENT_FAILED_CODE = 242; //DFS读取文件内容失败
	public static final int ADD_FORWARD_RULES_FAILED_CODE = 243; //转发规则添加失败
	public static final int CONNECTION_NOT_KEY_CODE = 244; //连接找不到key
	public static final int DFS_READ_FILE_FAILED_CODE = 245; //DFS 读取文件失败
	public static final int CONF_DECODE_EXCEPTION_CODE = 246; // 配置文件解码信息异常
	public static final int WEBSOCKET_IO_EXCEPTION_CODE = 247; // websocketIO异常
	public static final int JSON_OBJECT_EXCEPTION_CODE = 248; // json对象转换异常
	public static final int MAIL_URL_NULL_CODE = 249; // 邮箱地址为空
	public static final int DFS_CONF_EXCEPTION_CODE = 250; // dfs配置文件读取失败
	public static final int HTTP_SCAN_TYPE_LIST_EXCEPTION_CODE = 251; // httpScanTypeList is null
	public static final int ACCOUNT_BINDING_MAIL_OVERFLOE_LIMIT_CODE = 252; // 用户账号绑定邮箱数量超出上限阀值
	public static final int DATE_FORMAT_CODE = 253; // 日期格式化异常
	public static final int OBJECT_TO_JSON_EXCEPTION_CODE = 254; // 对象转换为json异常
	// ---------- error_code -------end--------
	// 执行成功
	public static final ErrorCodeContants SUCCESS = new ErrorCodeContants(
			SUCCESS_CODE, "success");

	// 系统级异常
	public static final ErrorCodeContants PASSWORD_INCORRECT = new ErrorCodeContants(
			PASSWORD_INCORRECT_CODE, "mail or password is incorrect"); // 网络连接异常
	public static final ErrorCodeContants EMAIL_LOGIN_FAILED = new ErrorCodeContants(
			EMAIL_LOGIN_FAILED_CODE, "email login failed, mail or password is incorrect"); // 邮箱登录失败,邮箱或密码错误
	public static final ErrorCodeContants SYSTEM_EXCEPTION = new ErrorCodeContants(
			SYSTEM_EXCEPTION_CODE, "system exception"); // 系统异常
	public static final ErrorCodeContants UNKNOWN_ERROR = new ErrorCodeContants(
			UNKNOWN_ERROR_CODE, "unknown error"); // 未知的错误
	public static final ErrorCodeContants POP_NOT_OPEN_EXCEPTION = new ErrorCodeContants(
			POP_NOT_OPEN_EXCEPTION_CODE, "email or password error or POP protocol is not open exception"); // pop协议未开启
	public static final ErrorCodeContants IMAP_NOT_OPEN_EXCEPTION = new ErrorCodeContants(
			IMAP_NOT_OPEN_EXCEPTION_CODE, "email or password error or imap protocol is not open exception"); // imap协议未开启
	public static final ErrorCodeContants HTTP_SSL_EXCEPTION = new ErrorCodeContants(
			HTTP_SSL_EXCEPTION_CODE, "HttpClient ssl exception"); 								// ssl异常

	// 业务级异常
	public static final ErrorCodeContants NOT_SUPPORT_BANK_TYPE = new ErrorCodeContants(
			NOT_SUPPORT_BANK_TYPE_CODE, "not support data type"); 								// 不支持该银行模板类型
	public static final ErrorCodeContants DECODE_EXCEPTION = new ErrorCodeContants(
			DECODE_EXCEPTION_CODE, "decode message exception"); 								// 解码信息异常
	public static final ErrorCodeContants CONF_DECODE_EXCEPTION = new ErrorCodeContants(
			CONF_DECODE_EXCEPTION_CODE, "config decode message exception"); 					// 配置文件解码信息异常
	public static final ErrorCodeContants ENCODE_EXCEPTION = new ErrorCodeContants(
			ENCODE_EXCEPTION_CODE, "encode message exception"); 								// 编码信息异常
	public static final ErrorCodeContants NOT_FOUND_MAIL_HANDLER = new ErrorCodeContants(
			NOT_FOUND_MAIL_HANDLER_CODE, "not found mail handler");								// 未找到邮件处理类
	public static final ErrorCodeContants BUSINESS_PROCESS_ERROR = new ErrorCodeContants(
			BUSINESS_PROCESS_ERROR_CODE, "business process error"); 							// 业务流程错误
	public static final ErrorCodeContants CAN_NOT_INVOKE = new ErrorCodeContants(
			CAN_NOT_INVOKE_CODE, "data type not support the method"); 							// 数据类型不支持该方法
	public static final ErrorCodeContants VALIDATE_ERROR = new ErrorCodeContants(
			VALIDATE_ERROR_CODE, "validate error"); 											// 验证错误
	public static final ErrorCodeContants ANALYSIS_DATA_EXCEPTION = new ErrorCodeContants(
			ANALYSIS_DATA_EXCEPTION_CODE, "data access exception"); 							// 数据解析异常
	public static final ErrorCodeContants MAIL_NOT_FOUND = new ErrorCodeContants(
			MAIL_NOT_FOUND_CODE, "mail not found"); 											// 邮箱地址未找到
	public static final ErrorCodeContants MAIL_FORMAT_EXCEPTION = new ErrorCodeContants(
			MAIL_FORMAT_EXCEPTION_CODE, "mail url is format error"); 							// 邮箱地址非法
	public static final ErrorCodeContants DATA_TYPE_CONVERSION = new ErrorCodeContants(
			DATA_TYPE_CONVERSION_CODE, "data type conversion error"); 							// 数据类型转换异常
	public static final ErrorCodeContants NOT_SUPPORT_EMAIL = new ErrorCodeContants(
			NOT_SUPPORT_EMAIL_CODE, "the mailbox is temporarily not compatible"); 				// 该邮箱暂时不支持异常
	public static final ErrorCodeContants EMAIL_INVALID_PROTOCOL = new ErrorCodeContants(
			EMAIL_INVALID_PROTOCOL_CODE, "Invalid mailbox protocol"); 							// 邮箱协议无效
	public static final ErrorCodeContants IO_EXCEPTION = new ErrorCodeContants(
			IO_EXCEPTION_CODE, "DFS : uploadFile failed"); 										// DFS的io 异常
	public static final ErrorCodeContants ADDRESSS_ISNULL = new ErrorCodeContants(
			ADDRESSS_ISNULL_CODE, "address is null"); 											// 地址未获取到异常
	public static final ErrorCodeContants RECEIVEDDATE_ISNULL = new ErrorCodeContants(
			RECEIVEDDATE_ISNULL_CODE, "receivedDate is null"); 									// 发件时间未获取到异常
	public static final ErrorCodeContants ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION = new ErrorCodeContants(
			ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION_CODE, "arrayIndexOutOfBounds exception"); 		// 数组越界异常
	public static final ErrorCodeContants JSON_FORMAT_EXCEPTION = new ErrorCodeContants(
			JSON_FORMAT_EXCEPTION_CODE, "@139.com json format date exception"); 				// json格式异常
	public static final ErrorCodeContants INVALID_KEYS_EXCEPTION = new ErrorCodeContants(
			INVALID_KEYS_EXCEPTION_CODE, "invalid Keys "); 										// invalid Keys 异常
	public static final ErrorCodeContants CIPHER_PADDING = new ErrorCodeContants(
			CIPHER_PADDING_CODE, "thrown when a particular padding mechanism is requested but is not available in the environment.");//cipher的获取实例异常
	public static final ErrorCodeContants ENCRYPTION_FAILED_EXCEPTION = new ErrorCodeContants(
			ENCRYPTION_FAILED_EXCEPTION_CODE, "a particular cryptographic algorithm is requested failed");// cipher的获取实例后加密异常
	public static final ErrorCodeContants UNSUPPORTED_ENCODING_EXCEPTION = new ErrorCodeContants(
			UNSUPPORTED_ENCODING_EXCEPTION_CODE, "The Character Encoding is not supported.");	// 编码异常
	public static final ErrorCodeContants GETCONTENT_FAILED = new ErrorCodeContants(
			GETCONTENT_FAILED_CODE, "获取邮件内容失败！");											// 获取邮件内容失败
	public static final ErrorCodeContants PASSWORD_SIZE_EXCEPTION = new ErrorCodeContants(
			PASSWORD_SIZE_EXCEPTION_CODE, "illegal block size 异常");								// 密码加密数据不合理异常
	public static final ErrorCodeContants PASSWORD_ENCRYPTION_EXCEPTION = new ErrorCodeContants(
			PASSWORD_ENCRYPTION_EXCEPTION_CODE, "解密异常");										// 解密异常
	public static final ErrorCodeContants PASSWORD_DECRYPT3DES_EXCEPTION = new ErrorCodeContants(
			PASSWORD_DECRYPT3DES_EXCEPTION_CODE, "加密异常");										// 加密异常
	public static final ErrorCodeContants GETSUBJECT_FAILED = new ErrorCodeContants(
			GETSUBJECT_FAILED_CODE, "获取邮件主题异常");												// 获取邮件主题异常
	public static final ErrorCodeContants GETSENDER_FAILED_EXCEPTION = new ErrorCodeContants(
			GETSENDER_FAILED_EXCEPTION_CODE, "获取邮件发件人异常");									// 获取邮件发件人异常
	public static final ErrorCodeContants NO_SENDER = new ErrorCodeContants(
			NO_SENDER_CODE, "没有发件人异常");														// 没有邮件发件人异常
	public static final ErrorCodeContants GETEMAIL_ADDRESSS_FAILED = new ErrorCodeContants(
			GETEMAIL_ADDRESSS_FAILED_CODE, "获取邮件收件人、抄送和密送地址异常");							// 获取邮件收件人、抄送和密送地址
	public static final ErrorCodeContants NOT_FOUND_PARSED_MAIL = new ErrorCodeContants(
			NOT_FOUND_PARSED_MAIL_CODE, "未找到要解析的邮件!");										// 未找到要解析的邮件!
	public static final ErrorCodeContants SEARCH_FAILED = new ErrorCodeContants(
			SEARCH_FAILED_CODE, "pop过滤邮件异常");													// 过滤邮件异常
	public static final ErrorCodeContants GET_EMAIL_FOLDER_FAILED = new ErrorCodeContants(
			GET_EMAIL_FOLDER_FAILED_CODE, "获取邮件目录异常");										// 获取邮件目录异常
	public static final ErrorCodeContants NULL = new ErrorCodeContants(
			NULL_CODE, "空指针异常");																// 空指针异常
	public static final ErrorCodeContants SQL_UPDATE_FAILED = new ErrorCodeContants(
			SQL_UPDATE_FAILED_CODE, "违反唯一约束异常");												// 违反唯一约束异常
	public static final ErrorCodeContants DFS_UNKNOWN_ERROR = new ErrorCodeContants(
			DFS_UNKNOWN_ERROR_CODE, "DFS unknown error");										// DFS 未知异常
	public static final ErrorCodeContants File_Not_Found_EXCEPTION = new ErrorCodeContants(
			File_Not_Found_EXCEPTION_CODE, "file not found exception");							// 文件未找到异常
	public static final ErrorCodeContants NO_THIS_BILLING_TYPE_TEMPLATE = new ErrorCodeContants(
			NO_THIS_BILLING_TYPE_TEMPLATE_CODE, "暂时没有这个账单的模板");								// 暂时没有这个账单的模板
	public static final ErrorCodeContants INTERRUPTED_EXCEPTION = new ErrorCodeContants(
			INTERRUPTED_EXCEPTION_CODE, "interrupted exception");								// 线程等待异常
	public static final ErrorCodeContants SCRIPT_EXCEPTION = new ErrorCodeContants(
			SCRIPT_EXCEPTION_CODE, "script exception code");									// script脚本执行异常
	public static final ErrorCodeContants NOSUCH_METHOD_EXCEPTION = new ErrorCodeContants(
			NOSUCH_METHOD_EXCEPTION_CODE, "no such method exception");							// 未找到相应方法的异常
	public static final ErrorCodeContants DFS_GETCONTENT_FAILED = new ErrorCodeContants(
			DFS_GETCONTENT_FAILED_CODE, "DFS读取内容失败！");										// DFS读取文件内容失败
	public static final ErrorCodeContants ADD_FORWARD_RULES_FAILED = new ErrorCodeContants(
			ADD_FORWARD_RULES_FAILED_CODE, "转发规则添加失败！");										// 转发规则添加失败
	public static final ErrorCodeContants CONNECTION_NOT_KEY = new ErrorCodeContants(
			CONNECTION_NOT_KEY_CODE, "action onClose(closeCode, message) not found key");		// 连接时找不到key
	public static final ErrorCodeContants DFS_READ_FILE_FAILED = new ErrorCodeContants(
			DFS_READ_FILE_FAILED_CODE, "DFS读取文件失败！");											// DFS读取文件失败！
	public static final ErrorCodeContants WEBSOCKET_IO_EXCEPTION = new ErrorCodeContants(
			WEBSOCKET_IO_EXCEPTION_CODE, "websocket io error");									// websocket io error
	public static final ErrorCodeContants JSON_OBJECT_EXCEPTION = new ErrorCodeContants(
			JSON_OBJECT_EXCEPTION_CODE, "json object conversion error");						// json对象转换异常
	public static final ErrorCodeContants MAIL_URL_NULL = new ErrorCodeContants(
			MAIL_URL_NULL_CODE, "mail url is null");											// 邮箱地址为空
	public static final ErrorCodeContants DFS_CONF_EXCEPTION = new ErrorCodeContants(
			DFS_CONF_EXCEPTION_CODE, "DFS config path failed");									// 邮箱地址为空
	public static final ErrorCodeContants HTTP_SCAN_TYPE_LIST_EXCEPTION = new ErrorCodeContants(
			HTTP_SCAN_TYPE_LIST_EXCEPTION_CODE, "httpScanTypeList is null");					// httpScanTypeList is null
	public static final ErrorCodeContants ACCOUNT_BINDING_MAIL_OVERFLOE_LIMIT = new ErrorCodeContants(
			ACCOUNT_BINDING_MAIL_OVERFLOE_LIMIT_CODE, "account binding mail overflow limit");	// 用户账号绑定邮箱数量超出上限阀值
	public static final ErrorCodeContants DATE_FORMAT_EXCEPTION = new ErrorCodeContants(		// 日期格式化异常
			DATE_FORMAT_CODE, "日期格式化异常"); 	
	public static final ErrorCodeContants OBJECT_TO_JSON_EXCEPTION = new ErrorCodeContants(
			OBJECT_TO_JSON_EXCEPTION_CODE, "object to json error"); 							// 对象转换为json异常
	
	private int code;
	private String msg;

	public ErrorCodeContants(int code, String msg) {
		log.info("class:{} \tcode:{} \tmsg:{}",
				new Object[] { this.getClass(), code, msg });
		this.code = code;
		this.msg = msg;
	}

	public boolean isSuccess() {
		return this.equals(SUCCESS) || code == SUCCESS.code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
