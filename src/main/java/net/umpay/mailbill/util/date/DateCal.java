package net.umpay.mailbill.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import net.umpay.mailbill.util.exception.MailBillException;

/**
 * 一些日期运算的方法
 */

public class DateCal {
	GregorianCalendar gCalendar = null;

	public DateCal() {
		gCalendar = new GregorianCalendar();
	}

	public DateCal(String strDate) {
		try {
			DateFormat dateFormat = DateFormat.getDateInstance(2, Locale.CHINA);
			dateFormat.parse(strDate);
			Calendar calendar = dateFormat.getCalendar();
			gCalendar = new GregorianCalendar(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
			System.out.println("exchange=" + exchange());
		} catch (ParseException e) {
			System.out.println("Unable to parse " + strDate);

		}
	}

	/**
	 * 计算指定的两个日期间隔的天数
	 * 
	 * @param startDate	开始日期
	 * @param endDate	结束日期
	 * @return 间隔天数
	 * @exception WorkFlowException
	 */
	public static String interval(String startDate, String endDate) throws ParseException {
		String str = "";

		GregorianCalendar startGregorianCalendar = DateUtil.parseString(startDate);
		GregorianCalendar endGregorianCalendar = DateUtil.parseString(endDate);
		// 如果两个日期相等
		if (startGregorianCalendar.equals(endGregorianCalendar)) {
			return "0";
		}// end if
			// 如果endGregorianCalendar日期小于startGregorianCalendar日期，则交换
		if (startGregorianCalendar.after(endGregorianCalendar)) {
			GregorianCalendar temp = startGregorianCalendar;
			startGregorianCalendar = endGregorianCalendar;
			endGregorianCalendar = temp;
		}// end if
			//
		int count = 1;
		while (true) {
			// 开始累加
			startGregorianCalendar.add(GregorianCalendar.DATE, 1);
			if (startGregorianCalendar.equals(endGregorianCalendar))
				break; // 累加到相等则退出
			else
				count++; // 间隔天数自加
		}// end while
		str = String.valueOf(count);

		return str;
	}

	/**
	 * 在指定的日期上加上指定的天数。
	 * 
	 * @param num
	 *            天数。
	 * @return YYYY-MM-DD字符串。
	 * @exception WorkFlowException
	 *                。
	 */
	public static String addDay(GregorianCalendar gCalendar, String num) {
		if (num == null || num.trim().equals(""))
			num = "0";
		gCalendar.add(GregorianCalendar.DATE, Integer.valueOf(num.trim())
				.intValue());
		return DateUtil.getFormatDate(gCalendar);
	}

	/**
	 * 在当前日期上加上指定的天数。
	 * 
	 * @param num
	 *            天数。
	 * @return YYYY-MM-DD字符串。
	 * @exception WorkFlowException
	 *                。
	 */
	public static String addDate(String num) {
		if (num == null || num.trim().equals(""))
			num = "0";
		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.add(GregorianCalendar.DATE, Integer.valueOf(num.trim())
				.intValue());
		return DateUtil.getFormatDate(gCalendar);
	}

	/**
	 * 在当前日期上加上指定的天数，除去历法中的周六、周日。
	 * 
	 * @param num
	 *            天数。
	 * @return YYYY-MM-DD字符串。
	 * @exception WorkFlowException
	 *                。
	 */
	public static String addDateExceptWeekend(String num) {
		String str = "";

		GregorianCalendar gCalendar = new GregorianCalendar();
		for (int i = 1; i <= Integer.valueOf(num).intValue(); i++) {
			gCalendar.add(GregorianCalendar.DATE, 1);
			int t = gCalendar.get(Calendar.DAY_OF_WEEK);
			while (t == Calendar.SATURDAY || t == Calendar.SUNDAY) {
				gCalendar.add(GregorianCalendar.DATE, 1);
				t = gCalendar.get(Calendar.DAY_OF_WEEK);
			}// end while
		}// end for
		str = DateUtil.getFormatDate(gCalendar);

		return str;
	}

	/**
	 * 在指定的日期上加上指定的天数，除去历法中的周六、周日。
	 * 
	 * @param num
	 *            天数。
	 * @return YYYY-MM-DD字符串。
	 * @exception WorkFlowException
	 *                。
	 */
	public static String addDateExceptWeekend(String strDate, String num)
			throws ParseException {
		String str = "";

		GregorianCalendar gCalendar = DateUtil.parseString(strDate);
		for (int i = 1; i <= Integer.valueOf(num).intValue(); i++) {
			gCalendar.add(GregorianCalendar.DATE, 1);
			int t = gCalendar.get(Calendar.DAY_OF_WEEK);
			while (t == Calendar.SATURDAY || t == Calendar.SUNDAY) {
				gCalendar.add(GregorianCalendar.DATE, 1);
				t = gCalendar.get(Calendar.DAY_OF_WEEK);
			}// end while
		}// end for
		str = DateUtil.getFormatDate(gCalendar);

		return str;
	}

	/**
	 * 加上指定的月数
	 * 
	 * @param month 	月数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String addMonth(int month) {
		gCalendar.add(GregorianCalendar.MONTH, month);
		return exchange();
	}

	/**
	 * 加上指定的年数
	 * 
	 * @param year		年数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String addYear(int year) {
		gCalendar.add(GregorianCalendar.YEAR, year);
		return exchange();
	}

	/**
	 * 加上指定的星期数
	 * 
	 * @param week		星期数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String addWeek(int week) {
		gCalendar.add(GregorianCalendar.WEEK_OF_MONTH, week);
		return exchange();
	}

	/**
	 * 减去指定的天数
	 * 
	 * @param day		天数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String decDay(int day) {
		day = -1 * day;
		gCalendar.add(GregorianCalendar.DATE, day);
		return exchange();
	}

	/**
	 * 减去指定的月数
	 * 
	 * @param month		月数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String decMonth(int month) {
		month = -1 * month;
		gCalendar.add(GregorianCalendar.MONTH, month);
		return exchange();
	}
	
	/**
	 * 指定日期减去指定的月数加上一天
	 * 		2014/3/24--->2014/4/23
	 * 		2014年02月07日-2014年03月06日
	 * @param date		日期字符串
	 * @param month		月数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	
	@SuppressWarnings("static-access")
	public static String decMonth(String date, int month) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();//日历对象
		calendar.setTime(DateUtil.stringToDate(date));//	2014/3/22--->2014/4/23
		calendar.add(calendar.MONTH, month);
		calendar.add(calendar.DAY_OF_YEAR, 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * 减去指定的年数
	 * 
	 * @param year		年数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String decYear(int year) {
		year = -1 * year;
		gCalendar.add(GregorianCalendar.YEAR, year);
		return exchange();
	}

	/**
	 * 减去指定的星期数
	 * 
	 * @param week		星期数
	 * @return 日期字符串(YYYY-MM-DD)
	 */
	public String decWeek(int week) {
		week = -1 * week;
		gCalendar.add(GregorianCalendar.WEEK_OF_MONTH, week);
		return exchange();
	}

	/**
	 * 判断是否为工作日（星期六、星期日）
	 * 
	 * @return true--工作日；false--星期六、星期日
	 */
	public boolean judgeWorkDate() {
		int j = gCalendar.get(Calendar.DAY_OF_WEEK);
		if ((j == Calendar.SUNDAY) || (j == Calendar.SATURDAY)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 将日期转换为"YYYY-MM-DD"形式输出
	 */
	private String exchange() {
		String sYear = "", sMonth = "", sDay = "";
		//
		int year = gCalendar.get(Calendar.YEAR);
		int month = gCalendar.get(Calendar.MONTH);
		int day = gCalendar.get(Calendar.DAY_OF_MONTH);
		//
		sYear = String.valueOf(year); // 年
		month = month + 1; // 月
		if (month < 10) {
			sMonth = "0" + String.valueOf(month);
		} else {
			sMonth = String.valueOf(month);
		}
		if (day < 10) // 日
		{
			sDay = "0" + String.valueOf(day);
		} else {
			sDay = String.valueOf(day);
		}
		//
		return sYear + "-" + sMonth + "-" + sDay;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws MailBillException {
		Date date = new Date();
		DateCal dateCal = new DateCal(date.toLocaleString());
		String decDay = dateCal.decDay(2);
//		String interval = DateCal.interval("2014-12-24", date.toLocaleString());
		System.out.println(dateCal);
		System.out.println(decDay);
	}
};