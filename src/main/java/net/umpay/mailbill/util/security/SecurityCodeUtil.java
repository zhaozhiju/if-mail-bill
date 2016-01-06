package net.umpay.mailbill.util.security;

import java.util.UUID;

public class SecurityCodeUtil {
	
	/***
	 * 生成验证码
	 */
	public static String encode(){
		String uuid=UUID.randomUUID().toString();
		uuid=uuid.replaceAll("-", "");
		return uuid;
	}
	
	
	public static void main(String[] args){
		String uuid=UUID.randomUUID().toString();
		System.out.println("uuid="+uuid.replaceAll("-", ""));
		
		System.out.println(uuid.substring(0,2));
		System.out.println(uuid.substring(2,uuid.length()));
		String accountUserInfoId="test";
		uuid=uuid.substring(0,2)+"_"+accountUserInfoId+"_"+uuid.substring(2,uuid.length());
		System.out.println("uuid["+uuid+"]");
		
	}
}
