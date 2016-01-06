package net.umpay.mailbill.util.number;

/**
 * 基本计算(用于财务货币)
 */
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Arith {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(Arith.class);
	
	private static final int DEF_DIV_SCALE = 10;

	/**
	 * 结果是： -1 v1小于v2 0 v1等于v2 1 v1大于v2
	 * 
	 * @param v1
	 * @param v2
	 * @return	int
	 */
	public static int compareTo(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		int result = b1.compareTo(b2);
		return result;
	}

	/**
	 * 提供精确的加法运算。
	 * 
	 * @param v1
	 *            被加数
	 * @param v2
	 *            加数
	 * @return 两个参数的和
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.add(b2).doubleValue();
	}

	/**
	 * 提供精确的减法运算。
	 * 
	 * @param v1
	 *            被减数
	 * @param v2
	 *            减数
	 * @return 两个参数的差
	 */
	public static double sub(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 提供精确的乘法运算。
	 * 
	 * @param v1
	 *            被乘数
	 * @param v2
	 *            乘数
	 * @return 两个参数的积
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到小数点以后10位，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @param scale
	 *            表示表示需要精确到小数点以后几位。
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(v);
		// BigDecimal one = new BigDecimal(1.00d);
		return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	// -----------------------------------

	/**
	 * 提供精确的加法运算。
	 * 
	 * @param v1
	 *            被加数
	 * @param v2
	 *            加数
	 * @return 两个参数的和
	 */
	public static String add(String v1, String v2) {
		String d1 = NumberTools.isNumber(v1) ? v1 : "0.00";
		String d2 = NumberTools.isNumber(v2) ? v2 : "0.00";
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		BigDecimal result = b1.add(b2);
		if (BigDecimal.ZERO.compareTo(result) == 0) {
			System.out.println("和为零");
			return "0";
		}
		return result.toString();
	}

	/**
	 * 提供精确的减法运算。
	 * 
	 * @param v1
	 *            被减数
	 * @param v2
	 *            减数
	 * @return 两个参数的差
	 */
	public static String sub(String v1, String v2) {
		String d1 = NumberTools.isNumber(v1) ? v1 : "0.00";
		String d2 = NumberTools.isNumber(v2) ? v2 : "0.00";
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		BigDecimal result = b1.subtract(b2);
		if (BigDecimal.ZERO.compareTo(result) == 0) {
			System.out.println("差为零");
			return "0";
		}
		return result.toString();
	}

	/**
	 * 提供精确的乘法运算。
	 * 
	 * @param v1
	 *            被乘数
	 * @param v2
	 *            乘数
	 * @return 两个参数的积
	 */
	public static String mul(String v1, String v2) {
		String d1 = NumberTools.isNumber(v1) ? v1 : "0.00";
		String d2 = NumberTools.isNumber(v2) ? v2 : "0.00";
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		BigDecimal result = b1.multiply(b2);
		if (BigDecimal.ZERO.compareTo(result) == 0) {
			System.out.println("积为零");
			return "0";
		}
		return result.toString();
	}

	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @return 两个参数的商
	 */

	public static String div(String v1, String v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @param scale
	 *            表示表示需要精确到小数点以后几位。
	 * @return 两个参数的商
	 */
	public static String div(String v1, String v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		String d1 = NumberTools.isNumber(v1) ? v1 : "0.00";
		String d2 = NumberTools.isNumber(v2) ? v2 : "0.00";
		BigDecimal b1 = new BigDecimal(d1);
		BigDecimal b2 = new BigDecimal(d2);
		if (BigDecimal.ZERO.compareTo(b2) == 0) {
			b2 = new BigDecimal("1");
		}
		BigDecimal result = b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
		if (BigDecimal.ZERO.compareTo(result) == 0) {
			System.out.println("商为零");
			return "0";
		}
		return result.toString();
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @return 四舍五入后的结果
	 */
	public static String round(String v) {
		return round(v, DEF_DIV_SCALE);
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param ifZero
	 *            true非法返回0.00；而false返回“”
	 * @return 四舍五入后的结果
	 */
	public static String round(String v, boolean ifZero) {
		return round(v, DEF_DIV_SCALE, ifZero);
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static String round(String v, int scale) {
		return round(v, scale, true);
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @param ifZero
	 *            true非法返回0.00；而false返回“”
	 * @return 四舍五入后的结果
	 */
	public static String round(String v, int scale, boolean ifZero) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		try {
			// double d = Double.parseDouble(v);
			BigDecimal b = new BigDecimal(v);
			BigDecimal result = b.setScale(scale, BigDecimal.ROUND_HALF_UP);
			if (BigDecimal.ZERO.compareTo(result) == 0) {
				System.out.println("四舍五入为零");
				return "0";
			}
			return result.toString();
		} catch (RuntimeException e) {
			if (ifZero) {
				log.error("提供精确的小数位四舍五入处理时校验(非法默认为零)转换处理:{}", e.getMessage());
				return "0.00";
			} else {
				log.error("提供精确的小数位四舍五入处理时校验(非法默认为空)转换处理:{}", e.getMessage());
				return "";
			}
		}
	}

}
