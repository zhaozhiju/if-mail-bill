package net.umpay.mailbill.util.money;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.umpay.mailbill.util.number.Arith;

import org.apache.commons.lang.StringUtils;

public class Money {

	/**
	 * 静态方法，将用逗号格式化金额数据还原.<br>
	 * 入口参数：格式化金额数据.<br>
	 * 返回值：还原后的数据.<br>
	 */
	public static String moneyUnFormat(String money) {
		
		String intPart = "";

		// 是否为逗号分隔
		int i = money.indexOf(",");
		if (i == -1) {
			return money;
		}
		intPart = money;
		int pos = 0;
		while (true) {
			pos = intPart.indexOf(",");
			if (pos == -1) {
				break;
			}
			intPart = intPart.substring(0, pos)
					+ intPart.substring(pos + 1, intPart.length());
		}
		return intPart;
	}

	/**
	 * 静态方法，将金额数据用逗号格式化显示.<br>
	 * @param money			表示金额的字符串.
	 * @param roundLength	保留小数个数	
	 * 返回值：格式化后的字符串.<br>
	 * 
	 * 备注：小数点后保留三位小数，四舍五入
	 */
	public static String moneyFormat(String money, int roundLength) {
		String str = "";
		if (money.trim().length() > 0) {
			BigDecimal b1 = new BigDecimal(Arith.round(money, roundLength));
			double douMoney = b1.doubleValue();
			// NumberFormat 常用于指定不同于本地的地区
			NumberFormat numberFormat = NumberFormat.getInstance(Locale.CHINA);
			// DecimalFormat 常用于获得很好的格式控制
			DecimalFormat decimalFormat = null;
			try {
				decimalFormat = (DecimalFormat) numberFormat;
			} catch (ClassCastException e) {
				System.out.println(e.toString());
			}
			StringBuilder sbStr = new StringBuilder();
			sbStr.append("#,##0.");
			for(int i = 0; i < roundLength; i++){
				sbStr.append("0");
			}
			
			decimalFormat.applyPattern(sbStr.toString());
			str = decimalFormat.format(douMoney);
		}
		
		return str;
	}
	/**
	 * 静态方法，将金额数据用逗号格式化显示.<br>
	 * 入口参数：表示金额的字符串.<br>
	 * 返回值：格式化后的字符串.<br>
	 * 备注：小数点后保留两位小数，四舍五入
	 */
	public static String moneyFormat(String money) {
		String intPart = "";
		String decPart = "";
		String sign = "";
		if (money.indexOf(",") != -1) {
			return money;
		}
		if (money.equals("")) {
			return "";
		}
		if (money.equals("0")) {
			return "0.00";
		}
		// 判断是否为负数
		if (money.substring(0, 1).equals("-")) {
			sign = "-";
			money = money.substring(1, money.length());
		}
		//
		if (money.substring(0, 1).equals(".")) {
			money = "0" + money;
		}
		// 判断是否为指数形式
		int j = money.indexOf("E");
		if (j != -1) {
			// 指数
			int bitnum = Integer
					.valueOf(money.substring(j + 1, money.length())).intValue();
			// 底数
			String root = money.substring(0, j);
			// 右移
			if (bitnum > 0) {
				if (root.length() - 2 <= bitnum) {
					int c = bitnum - (root.length() - 2) + 1;
					for (int x = 0; x < c; x++) {
						root = root + "0";
					}
				}
				String str1 = root.substring(0, bitnum + 2) + "."
						+ root.substring(bitnum + 2, root.length());
				root = str1.substring(0, 1) + str1.substring(2, str1.length());
			}
			// 左移
			else {
				bitnum = Math.abs(bitnum);
				String str1 = root.substring(0, 1)
						+ root.substring(2, root.length());
				root = "0.";
				for (int c = 0; c < bitnum - 1; c++) {
					root = root + "0";
				}
				root = root + str1;
			}
			money = root;
		}// end if
			// 不为整数
		int indexof = money.indexOf(".");
		if (indexof != -1) {
			intPart = money.substring(0, indexof);
			decPart = money.substring(indexof + 1, money.length());
		} else {
			intPart = money;
			decPart = "00";
		}
		// 小数保留两位，四舍五入
		if (decPart.length() > 2) {
			String temp1 = decPart.substring(0, 2);
			String temp2 = decPart.substring(2, 3);
			int num2 = Integer.valueOf(temp2).intValue();
			int num1 = 0;
			// 五入
			if (num2 >= 5) {
				num1 = Integer.valueOf(temp1).intValue();
				if (num1 <= 8) {
					num1++;
					// 小数部分
					decPart = "0" + String.valueOf(num1);
				} else {
					num1++;
					if (num1 >= 100) {
						// 小数部分
						String temp3 = String.valueOf(num1);
						decPart = temp3.substring(1, 3);
						// 整数部分
						long num3 = Long.valueOf(intPart).longValue();
						num3++;
						intPart = String.valueOf(num3);
					} else {
						// 小数部分
						decPart = String.valueOf(num1);
					}// end if
				}// end if
			} else
			// 四舍
			{
				decPart = temp1;
			}
		}
		// 需要逗号分隔
		if (intPart.length() > 3) {
			String temp1 = intPart;
			String result = "";
			while (temp1.length() > 3) {
				result = ","
						+ temp1.substring(temp1.length() - 3, temp1.length())
						+ result;
				temp1 = temp1.substring(0, temp1.length() - 3);
			}
			result = temp1 + result;
			intPart = result;
		}
		// 若小数位数为1位，则统一位为2位
		if (decPart.length() == 1) {
			decPart = decPart + "0";
		}
		return sign + intPart + "." + decPart;
	}

	/**
	 * 采用四舍五入的方法格式化数据
	 * @param dblInput  	需要格式化的数据
	 * @param strFormat		格式如：##，###.00
	 * 
	 * 输出：String 经过格式化得来的数值
	 */
	public String toFormatNum(double dblInput, String strFormat) {
		try {
			if (dblInput < 0) {
				dblInput = dblInput - 0.0000001;
			}
			// DecimalFormat有BUDGE，当末位为5时无法正确进行四舍五入
			else if (dblInput > 0) {
				dblInput = dblInput + 0.0000001;
			}
			// DecimalFormat有BUDGE，当末位为5时无法正确进行四舍五入
			else {
				dblInput = 0;
			}

			DecimalFormat fmt = new DecimalFormat(strFormat); // "#,###,##0.00"
			strFormat = fmt.format(dblInput);
		} catch (Exception e) {
			System.out.println("Error in Utility.toFormatNum :" + e.toString());
		}
		return strFormat;
	}

	/**
	 * 采用四舍五入的方法格式化数据 
	 * @param strInput 		需要格式化的数据
	 * @param strFormat 	格式如：##，###.00
	 * 
	 * 输出：String 经过格式化得来的数值
	 */
	public String toFormatNum(String strInput, String strFormat) {
		if (StringUtils.isBlank(strInput)) {
			return "";
		} else {
			return toFormatNum(Double.parseDouble(strInput), strFormat);
		}
	}

	/**
	 * 将用字符串表示的数值转化为字符串，删除输入中的币种符号,百分号及空格 输入：$123,456,789.123 输出：123456789.123
	 */
	public static String toNumberic(String strInput) {
		String strReturn = strInput;
		try {
			if (StringUtils.isBlank(strInput)) {
				return "";
			}
			strInput = strInput.trim();
			
			int intDelete = 0; // 去掉“,”“$”,"￥","＄","￡","％","%", " "
			String strDelete = ","; // 1
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "$"; // 2
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "￥"; // 3
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "＄"; // 4
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "￡"; // 5
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "％"; // 6
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "%"; // 7
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "CNY"; // 8
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 3);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "(存入)"; // 9
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 4);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "&nbsp;"; // 9
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 6);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "RMB"; // 9
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 3);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = "USD"; // 9
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 3);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = ":"; // 9
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
			strDelete = " "; // 10
			intDelete = strReturn.indexOf(strDelete);
			while (intDelete != -1) {
				strReturn = strReturn.substring(0, intDelete)
						+ strReturn.substring(intDelete + 1);
				intDelete = strReturn.indexOf(strDelete);
			}
		} catch (Exception e) {
			System.out.println("Error in ChangeNumberic.toNumberic :"
					+ e.toString());
		}
		return strReturn;
	}

	public static String scientificFormat(String str) {
		if (str.trim().equals("")) {
			return "";
		}
		DecimalFormat d = new DecimalFormat("0.###E0");
		BigDecimal bigDecimal = new BigDecimal(str);
		return d.format(bigDecimal);
	}
	
	/**
	 * 剔除数字的前缀
	 * 
	 * @param moneyStr	待处理的数字字符串
	 * @return String
	 */
	public static String removePrefix(String moneyStr) {
		if (StringUtils.isNotBlank(moneyStr)) {
			if (moneyStr.startsWith("&yen;")) {
				return moneyStr.replaceAll("&yen;", "");
			}
			if (moneyStr.startsWith("$")) {
				int index = moneyStr.indexOf("$") + 1;
				return moneyStr.substring(index);
			}

			return moneyStr;
		}

		return null;
	}
	
	/**
	 * 获取字符串中的数字,并清除千分位
	 * 人民币rmb-1,234,567.987 - > -1234567.987
	 * 3,000.00/CNY - > 3000.00
	 * @param handleMoneyStr
	 * @return String
	 */
	public static String getNumber(String handleMoneyStr) {
		String nowStr = handleMoneyStr;
		if (StringUtils.isBlank(nowStr)) {
			return "0";
		}
		nowStr = Money.moneyUnFormat(nowStr);
		String regex = "-?\\d*";//"\\d*";
		Pattern p = Pattern.compile(regex);
		String dou = "";
		Matcher m = p.matcher(nowStr);

		while (m.find()) {
			if (StringUtils.isNotBlank(m.group())) {
				if (StringUtils.isNotBlank(dou)) {
					dou += "." + m.group();
					break;
				}
				dou = m.group();
			}
		}

		if(StringUtils.isNotBlank(dou) && dou.equals("-.-")){
			dou = "0";
		}
		return dou;
	}
	
	@SuppressWarnings("static-access")
	public static void main(String args[]) {
		Money money_util = new Money();
//		String str = money_util.toNumberic("CNY 2,333.345");
		String str11 = money_util.moneyUnFormat("RMB1,234.56");
		
//		System.out.println("ask8970".substring(2));
		System.out.println(str11);
		
		System.out.println(Money.removePrefix("$1000.00"));
		
//		System.out.println(Money.getNumber("人民币rmb1,234,567.987"));
		System.out.println(Money.getNumber("人民币rmb-1,234,567.987"));
		System.out.println(Money.getNumber("3,000.00/CNY"));
		System.out.println(Money.getNumber("$----"));
	}
}
