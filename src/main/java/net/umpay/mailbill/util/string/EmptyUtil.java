package net.umpay.mailbill.util.string;

import org.apache.commons.lang.StringUtils;

public class EmptyUtil {

	public static String emptyStringHandle(String cardNo){
		if(StringUtils.isBlank(cardNo)){
			cardNo = "";
		}
		return cardNo;
	}
}
