package net.umpay.mailbill.util.string;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取配置文件key-value键值对
 */
public class ReadProperty {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(ReadProperty.class);
	
	/**
	 * 获取配置文件中全部key-value键值对
	 * 
	 * @param filePath 	配置文件的相对路径
	 * @return 			配置文件中的key-value键值对
	 */
	
	public static Map<String, String> readProperties(String filePath) {
		Map<String, String> propVector=new HashMap<String, String>();
		Properties props = new Properties();
		try {
			InputStream in = new FileInputStream(filePath);
			props.load(in);
			Enumeration<?> en = props.propertyNames();
			// 在这里遍历
			while (en.hasMoreElements()) {
				String key = en.nextElement().toString();//key值
				propVector.put(key, StringUtils.isNotBlank(props.getProperty(key)) ? props.getProperty(key) : "");
			}
			return propVector;
		} catch (Exception e) {
			MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.CONF_DECODE_EXCEPTION_CODE, ErrorCodeContants.CONF_DECODE_EXCEPTION.getMsg(), log);
		}
		return null;
	}
	
	/**
	 * 获取邮件的来源邮箱地址
	 * @param path
	 * @return	邮箱地址
	 */
	public static String getEmailUrl(String path, int flag){
		String string = null;
		int k = 1;
		String[] split = path.split("/");
		for (String str : split){
			if (str.contains("@")){
				if (flag == k){
					return str;
				}
				k += 1;
			}
		}
		return string;
	}
	/**
	 * 获取邮件的相关信息
	 * @param path
	 * @param flag
	 * @return String
	 */
	public static String getEmailInfo(String path, int flag){
		String substring = path.substring(path.lastIndexOf("/")+1, path.length());
		String[] split = substring.split("`");
		if (split.length != 3){
			split[2] = split[split.length-1];
			split[1] = split[split.length-2];
			split[0] = split[split.length-3];
		}
		if (flag == 1){
			return split[1];
		}else if (flag == 2){
			String string = split[2];
			string = string.replaceAll(",", ":").substring(0, string.length()-5);
			string = string+":00";
			int indexOf = string.indexOf("星");
			if (indexOf != -1){
				string = string.substring(0, indexOf)+string.substring(indexOf + 3, string.length());
			}
			string = string.replaceFirst("年", "-");
			string = string.replaceFirst("月", "-");
			string = string.replaceFirst("日", "");
			return string;
		}else{
			String str = split[0];
			int lastIndexOf = str.lastIndexOf("/");
			str = str.substring(lastIndexOf+1, str.length());
			return str;
		}
	}
	
	public static void main(String[] args) {
		String emailUrl = ReadProperty.getEmailInfo("1/3/13311193275@163.com/service@vip.ccb.com/中国建设银行信用卡电子账单_xiuminchen@126.com_2014年04月03日 星期四 10,27.html",0);
//		String emailUrl = ReadProperty.getEmailUrl("1/3/13311193275@163.com/service@vip.ccb.com/中国建设银行信用卡电子账单_xiuminchen@126.com_2014年04月03日 星期四 10 27.html",2);
		System.out.println(emailUrl+"============");
//		Date stringToDateTime = DateUtil.stringToDate(emailUrl, "yyyy-MM-dd HH:mm:ss");
//		System.out.println(stringToDateTime);
//		Map<String, String> readProperties = ReadProperty.readProperties("./conf/queryKey.properties");
//		System.out.println(readProperties);
	}
	
}
