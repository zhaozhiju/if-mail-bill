package net.umpay.mailbill.util.date;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 获得当前系统时间
 */
public class CurrentTime {

	public CurrentTime() {
	}

	java.sql.Date date = null;

	/**
	 * 静态方法.<br>
	 * 获得当前日期（格式：YYYY-MM-DD）
	 */
	public static String getFullDay() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		String sMonth = "";
		String sDay = "";
		//
		int year = calendar.get(Calendar.YEAR); // 年
		//
		int month = calendar.get(Calendar.MONTH) + 1;// 月
		if (month < 10) {
			sMonth = "0" + String.valueOf(month);
		} else {
			sMonth = String.valueOf(month);
		}
		//
		int day = calendar.get(Calendar.DATE); // 日
		if (day < 10) {
			sDay = "0" + String.valueOf(day);
		} else {
			sDay = String.valueOf(day);
		}
		String toDay = year + "-" + sMonth + "-" + sDay;
		//
		return toDay;

	}

	/**
	 * 静态方法.<br>
	 * 获得当前时间（格式：HH:MM:SS）
	 */
	public static String getFullTime() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		String sHour = "";
		String sMinute = "";
		String sSecond = "";
		//
		int hour = calendar.get(Calendar.HOUR_OF_DAY); // 时
		// int hour=calendar.get(Calendar.HOUR); //时
		if (hour < 10) {
			sHour = "0" + String.valueOf(hour);
		} else {
			sHour = String.valueOf(hour);
		}
		//
		int minute = calendar.get(Calendar.MINUTE); // 分
		if (minute < 10) {
			sMinute = "0" + String.valueOf(minute);
		} else {
			sMinute = String.valueOf(minute);
		}
		//
		int second = calendar.get(Calendar.SECOND); // 秒
		if (second < 10) {
			sSecond = "0" + String.valueOf(second);
		} else {
			sSecond = String.valueOf(second);
		}
		//
		String str = sHour + ":" + sMinute + ":" + sSecond;
		return str;

	}

	/**
	 * 静态方法.<br>
	 * 获得当前完全时间（格式：YYYY-MM-DD HH:MM:SS）
	 */
	public static String getFullDateTime() {
		String str = getFullDay() + " " + getFullTime();
		return str;
	}

	/**
	 * 静态方法.<br>
	 * 获得当前是星期几？
	 */
	public static String getDayOfWeek() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		int day = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 星期
		String str = "";
		switch (day) {
		case 0:
			str = "Sunday";
			break;
		case 1:
			str = "Monday";
			break;
		case 2:
			str = "Tuesday";
			break;
		case 3:
			str = "Wednesday";
			break;
		case 4:
			str = "Thursday";
			break;
		case 5:
			str = "Friday";
			break;
		case 6:
			str = "Saturday";
			break;
		}
		return str;

	}

	/**
	 * 静态方法.<br>
	 * 获得当前是哪个月？
	 */
	public static String getMonthOfYear() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		int month = calendar.get(Calendar.MONTH); // 月
		String str = "";
		switch (month) {
		case 0:
			str = "January";
			break;
		case 1:
			str = "February";
			break;
		case 2:
			str = "March";
			break;
		case 3:
			str = "April";
			break;
		case 4:
			str = "May";
			break;
		case 5:
			str = "June";
			break;
		case 6:
			str = "July";
			break;
		case 7:
			str = "August";
			break;
		case 8:
			str = "September";
			break;
		case 9:
			str = "October";
			break;
		case 10:
			str = "November";
			break;
		case 11:
			str = "December";
			break;
		}
		return str;
	}

	/**
	 * 静态方法.<br>
	 * 获得当前是哪一年？
	 */
	public static String getYear() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone("Asia/Shanghai"));
		int year = calendar.get(Calendar.YEAR);
		return String.valueOf(year);
	}
}