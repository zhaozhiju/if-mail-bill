package net.umpay.mailbill.util.number;

import java.text.DecimalFormat;

public class NumberToString {
	/**
	 * 把double类型的转换成字符串
	 * 避免因为double数字过大导致的出现科学计数法的问题
	 * 例如 double d = 3.286369523456E7;直接转换成的字符串里面会带有E7这样的字符
	 * 例如对于以上的输入值"3.286369523456E7"，返回"32863695.23"
	 * 
	 * @param d 		合法的double值
	 * @return String	保留小数点后面2位，格式为"0.00"的字符串,
	 *	
	 */
	public static String getString(double d){
		return getString(d,"0.00");
	}
	
	/**
	 * 把double类型的转换成字符串
	 * 避免因为double数字过大导致的出现科学计数法的问题
	 * 例如 double d = 3.286369523456E7;直接转换成的字符串里面会带有E7这样的字符。
	 * 例如,对于输入值3.286369523456E7
	 * "0.00"保留小数点后面2位小数,返回"32863695.23"
	 * "0.0000"保留小数点后面4位小数,返回"32863695.2345"
	 * "0,0000.0000" 使用逗号分隔,保留4位小数返回："3286,3695.2346"
	 * @param d				合法的double值
	 * @param strPattern	字符串的格式,
	 * @return String		按格式strPattern处理后的字符串
	 */
	public static String getString(double d, String strPattern){
		return new DecimalFormat(strPattern).format(d).toString();
	}

}
