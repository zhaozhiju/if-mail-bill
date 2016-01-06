package net.umpay.mailbill.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日期的工具类
 */
public class DateUtil {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(DateUtil.class);

	public final static int EQUAL = 0;
	public final static int EARLY = -1;
	public final static int LATER = 1;
	
	public final static String FORMAT_YYYY_MM = "yyyy-MM";
	public final static String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	public final static String FORMAT_YYYY_MM_DD_HH_MM_SS= "yyyy-MM-dd HH:mm:ss";
	public final static String FORMATYYYYMMDD_HH_MM_SS = "yyyyMMdd HH:mm:ss";
	
	private static String DEFAULT_DATE_FORMAT1 = FORMAT_YYYY_MM;
	private static String DEFAULT_DATE_FORMAT = FORMAT_YYYY_MM_DD;
	private static String DEFAULT_TIME_FORMAT = FORMAT_YYYY_MM_DD_HH_MM_SS;
	private static String TIME_FORMAT = FORMATYYYYMMDD_HH_MM_SS;
	
	/**
	 *	相应的字符串日期转成Date类型
	 * @param dateStr	yyyy-mm-dd的日期类字符串
	 * @return	Date类型
	 */
	public static Date stringToDate(String dateStr) {
		if(StringUtils.isBlank(dateStr)){
			return null;
		}
		// 占位符
		if(dateStr.equalsIgnoreCase("&amp;nbsp;")){
			return null;
		}
		dateStr = getDateformat(dateStr);
		return stringToDate(dateStr, DEFAULT_DATE_FORMAT);
	}
	/**
	 *	相应的字符串日期转成Date类型
	 * @param dateStr	yyyy-mm-dd的日期类字符串
	 * @return	Date类型
	 */
	public static Date stringToDateTime(String dateStr) {
		dateStr = getDateformat(dateStr);
		if (dateStr.indexOf("-") == -1){
			return stringToDate(dateStr, TIME_FORMAT);
		}
		return stringToDate(dateStr, DEFAULT_TIME_FORMAT);
	}

	/**
	 * 将yyyy年mm月dd日的，还有yyyy/mm/dd的转换成
	 * 	yyyy-MM-dd
	 * @param dateStr  yyyy年mm月dd日     yyyy/mm/dd
	 * @return yyyy-MM-dd
	 */
	private static String getDateformat(String dateStr) {
		int indexOf = dateStr.indexOf("年");
		int indexOf1 = dateStr.indexOf("月");
		int indexOf2 = dateStr.indexOf("日");
		int indexOf3 = dateStr.indexOf("/");
		
		if (-1 != indexOf){
			dateStr = dateStr.replace(dateStr.charAt(indexOf), '-');
		}
		if (-1 != indexOf1){
			dateStr = dateStr.replace(dateStr.charAt(indexOf1), '-');
		}
		if (-1 != indexOf2){
			dateStr = dateStr.replace(dateStr.charAt(indexOf2), ' ');
		}
		if (-1 != indexOf3){
			dateStr = dateStr.replace(dateStr.charAt(indexOf3), '-');
		}
		return dateStr;
	}
	
	/** 
	 * 年月的日期格式转换
	 * return yyyy-mm
	 *  */
	public static Date string2DateMonth(String dateStr) {
		int index1 = dateStr.indexOf("（");
		int index2 = dateStr.indexOf("）");
		if (-1 != index1 && -1 != index2){
			dateStr = dateStr.replace(dateStr.charAt(index1), ' ');
			dateStr = dateStr.replace(dateStr.charAt(index2), ' ');
		}
		int indexOf = dateStr.indexOf("年");
		int indexOf3 = dateStr.indexOf("月份");
		if (-1 != indexOf3 && -1 != indexOf){
			dateStr = dateStr.replace(dateStr.charAt(indexOf), '-');
			dateStr = dateStr.replace(dateStr.charAt(indexOf3), ' ');
			return stringToDate(dateStr, DEFAULT_DATE_FORMAT1);
		}
		int index11 = dateStr.indexOf("年");
		int index21 = dateStr.indexOf("月");
		int index23 = dateStr.indexOf("日");
		if (-1 != index11 && -1 != index21 && -1 != index23){
			dateStr = getDateformat(dateStr);
			return stringToDate(dateStr, DEFAULT_DATE_FORMAT1);
		}

		if (-1 != index11 && -1 != index21){
			dateStr = dateStr.replace(dateStr.charAt(indexOf), '-');
			dateStr = dateStr.replace(dateStr.charAt(indexOf3), ' ');
			return stringToDate(dateStr, DEFAULT_DATE_FORMAT1);
		}
		
		dateStr = getDateformat(dateStr);
		return stringToDate(dateStr, DEFAULT_DATE_FORMAT1);
	}
	
	/** 
	 * 取Day日期的格式转换
	 * return day
	 *  */
	public static int string2Day(String dateStr) {
		
		int indexOf = dateStr.indexOf("每月");
		int indexOf3 = dateStr.indexOf("日");
		if (-1 != indexOf3 && -1 != indexOf){
			dateStr = dateStr.substring(indexOf+2, indexOf3);
			return Integer.valueOf(dateStr);
		}
		//日期xxxx/xx/xx
		int indexOf2 = dateStr.indexOf("/");
		if (-1 != indexOf2){
			dateStr = dateStr.replace(dateStr.charAt(indexOf2), '-');
			int lastIndexOf = dateStr.lastIndexOf("-");
			dateStr = dateStr.substring(lastIndexOf+1, dateStr.length());
			char charAt = dateStr.charAt(0);
			if (charAt == '0'){
				dateStr = dateStr.substring(dateStr.length()-1, dateStr.length());
			}
			return Integer.valueOf(dateStr);
		}
		//日期xxxx-xx-xx
		int index = dateStr.indexOf("-");
		if (-1 != index){
			int lastIndexOf = dateStr.lastIndexOf("-");
			dateStr = dateStr.substring(lastIndexOf+1, dateStr.length());
			char charAt = dateStr.charAt(0);
			if (charAt == '0'){
				dateStr = dateStr.substring(dateStr.length()-1, dateStr.length());
			}
			return Integer.valueOf(dateStr);
		}
		//日期XXXX年xx月xx日
		int indexOf4 = dateStr.indexOf("年");
		int indexOf5 = dateStr.indexOf("月");
		if (-1 != indexOf4 && -1 != indexOf3 && -1 != indexOf5){
			dateStr = dateStr.replace(dateStr.charAt(indexOf4), '-');
			dateStr = dateStr.replace(dateStr.charAt(indexOf5), '-');
			dateStr = dateStr.replace(dateStr.charAt(indexOf3), ' ');
			//截取日
			int lastIndexOf = dateStr.lastIndexOf("-");
			dateStr = dateStr.substring(lastIndexOf+1,dateStr.length()-1);
			//首位是0的去掉
			char charAt = dateStr.charAt(0);
			if (charAt == '0'){
				dateStr = dateStr.substring(dateStr.length()-1, dateStr.length());
			}
			return Integer.valueOf(dateStr);
		}
		if (index == -1 && indexOf2 == -1 && -1 == indexOf4 && -1 == indexOf3 && -1 == indexOf5 && dateStr.length() == 8){
			dateStr = dateStr.substring(dateStr.length()-2, dateStr.length());
			return Integer.valueOf(dateStr);
		}
		if (dateStr.length() <= 2){
			return Integer.valueOf(dateStr);
		}
		return -1;
	}
	
	/**
	 * 将日期字符串转换为指定的格式的Date
	 * 
	 * @param dateStr 	日期类字符串
	 * @param format	指定格式
	 */
	public static Date stringToDate(String dateStr, String format) {
		if(StringUtils.isBlank(dateStr)){
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setLenient(false);
		Date date = null;
		try {
			if(dateStr.length() >=20){ // 只截取前后信息,形如:yyyy-MM-dd  HH:mm:ss 或 yyyy-MM-dd Mon HH:mm:ss 
				StringBuffer newStr = new StringBuffer();
				newStr.append(dateStr.substring(0, 10));
				newStr.append(" ");
				newStr.append(dateStr.substring(dateStr.lastIndexOf(" ")+1));
				date = dateFormat.parse(newStr.toString());
			} else {
				date = dateFormat.parse(dateStr);
			}
		} catch (ParseException e) {
			log.error("date_error \tdateStr:{} \tformat:{}", new Object[]{dateStr, format});
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.DATE_FORMAT_CODE, ErrorCodeContants.DATE_FORMAT_EXCEPTION.getMsg(), log);
		}
		
		return date;
	}

	/**
	 * 判断时间字符串是否合法 "05.10.1981" // swiss date format (dd.MM.yyyy) "05-10-1981"
	 * "07-09-2006 23:00:33" "2006-09-07 23:01:25" "2003-08-30" //(yyyy.MM.dd)
	 * "2003-30-30" // false "some text" // false
	 * 
	 * @param date
	 * @return	boolean
	 */
	public static boolean isDate(String date) {
		// some regular expression
		String time = "(\\s(([01]?\\d)|(2[0123]))[:](([012345]\\d)|(60))"
				+ "[:](([012345]\\d)|(60)))?"; // with a space before, zero or
												// one time

		// no check for leap years (Schaltjahr)
		// and 31.02.2006 will also be correct
		String day = "(([12]\\d)|(3[01])|(0?[1-9]))"; // 01 up to 31
		String month = "((1[012])|(0\\d))"; // 01 up to 12
		String year = "\\d{4}";

		// define here all date format
		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		patterns.add(Pattern.compile(day + "[-.]" + month + "[-.]" + year
				+ time));
		patterns.add(Pattern.compile(year + "-" + month + "-" + day + time));
		// here you can add more date formats if you want

		// check dates
		for (Pattern p : patterns)
			if (p.matcher(date).matches())
				return true;

		return false;
	}

	/**
	 * 把Date类型转换成一定格式的字符串
	 * 
	 * @param myDate	java.util.Date
	 * @return			按"yyyy-MM-dd"格式化返回的字符串
	 */
	public static String getFormatDate(java.util.Date myDate) {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		return formatter.format(myDate);
	}

	/**
	 * 把Date类型转换成一定格式的字符串
	 * 
	 * @param myDate	java.util.Date类型的日期值
	 * @param strFormat	字符串的格式,例如"yyyy-MM-dd"
	 * @return			按传入字符串格式化后返回的字符串,如果传入的格式为空,则按照默认的格式返回
	 */
	public static String getFormatDate(java.util.Date myDate, String strFormat) {
		if(null == myDate){
			return "";
		}
		java.text.SimpleDateFormat formatter = null;
		if (StringUtils.isBlank(strFormat)) {
			formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
		} else {
			formatter = new java.text.SimpleDateFormat(strFormat);
		}
		return formatter.format(myDate);
	}

	/**
	 * 将日期格式化为"YYYY-MM-DD"字符串
	 * 
	 * @param gCalendar	GregorianCalendar对象
	 * @return 			"YYYY-MM-DD"字符串
	 */
	public static String getFormatDate(GregorianCalendar gCalendar) {
		java.util.Date date = gCalendar.getTime();
		SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return bartDateFormat.format(date);
	}

	/**
	 * 将日期"YYYY-MM-DD"字符串解析为GregorianCalendar对象
	 */
	public static GregorianCalendar parseString(String dateStr)
			throws ParseException {
		DateFormat dateFormat = DateFormat.getDateInstance(2, Locale.CHINA);
		dateFormat.parse(dateStr);
		Calendar calendar = dateFormat.getCalendar();
		return new GregorianCalendar(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));

	}

	/**
	 * 比较两个日期的先后关系
	 * 
	 * @param startDate		开始日期字符串 (YYYY-MM-DD)
	 * @param endDate		结束日期字符串(YYYY-MM-DD)
	 * @return 				整型。EQUAL--两个日期相等；EARLY--startDate在endDate之前；LATER--startDate在endDate之后
	 */
	public static int compare(String startDate, String endDate)
			throws ParseException {
		DateFormat dateFormat = DateFormat.getDateInstance(2, Locale.CHINA);
		java.util.Date date = dateFormat.parse(startDate);
		java.util.Date date1 = dateFormat.parse(endDate);
		return date.compareTo(date1);
	}

	public static void main(String[] args) {
		// System.out.println(DateUtil.compare("2009-04-05", "2009-03-21"));
		// System.out.println(DateUtil.compare("2009-04-05", "2009-04-05"));
		// System.out.println(DateUtil.compare("2009-04-05", "2009-08-21"));
		try {
			
			System.out.println(DateUtil.stringToDate("20100407","yyyyMMdd"));
//			System.out.println(DateUtil.isDate("2010-04-07"));
//			System.out.println(DateUtil.parseString("2010年04月07日"));
			System.out.println(DateUtil.string2Day("2010/04/07"));
			System.out.println(DateUtil.string2Day("2010年04月07日"));
			System.out.println(DateUtil.string2Day("每月7日"));
//			String s = "  34 ";
//			s=s.replaceAll(" ", "");
//			System.out.println(s);
			
			
			String dd = "2014-11-10 Mon 15:12:00";
			System.out.println(dd.length());
			System.out.println(dd.substring(0, 10));
//			System.out.println(dd.substring(15));
			System.out.println(dd.lastIndexOf(" ")+1);
			StringBuffer newStr = new StringBuffer();
			newStr.append(dd.substring(0, 10));
			newStr.append(" ");
			newStr.append(dd.substring(dd.lastIndexOf(" ")+1));
			System.out.println(newStr.toString());
			Date date = DateUtil.stringToDate(newStr.toString(), "yyyy-MM-dd HH:mm:ss");
			System.out.println(".."+date.toString());
			System.out.println(new java.util.Date());
			//-------------
			String dateStr = "2014年11/12日";
			int indexOf = dateStr.indexOf("年");
			int indexOf1 = dateStr.indexOf("月");
			int indexOf2 = dateStr.indexOf("日");
			int indexOf3 = dateStr.indexOf("/");
			
			if (-1 != indexOf){
				dateStr = dateStr.replace(dateStr.charAt(indexOf), '-');
			}
			if (-1 != indexOf1){
				dateStr = dateStr.replace(dateStr.charAt(indexOf1), '-');
			}
			if (-1 != indexOf2){
				dateStr = dateStr.replace(dateStr.charAt(indexOf2), ' ');
			}
			if (-1 != indexOf3){
				dateStr = dateStr.replace(dateStr.charAt(indexOf3), '-');
			}
			
			System.out.println("=="+dateStr);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 //截取数字  
	   public static String getNumbers(String content) {  
	       Pattern pattern = Pattern.compile("\\d+");  
	       Matcher matcher = pattern.matcher(content);  
	       while (matcher.find()) {  
	           return matcher.group(0);  
	       }  
	       return "";  
	   } 

}
