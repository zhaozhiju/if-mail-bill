package net.umpay.mailbill.util.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * 各种hash算法的帮助类 
 *
 */
public class HashUtil {

	public static String getSHA(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
			String digest = Base64
					.encodeBase64String(md.digest(text.getBytes()));
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// ignored
			return null;
		}

	}

	public static String getMD5(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			String digest = Base64
					.encodeBase64String(md.digest(text.getBytes()));
			return digest;
		} catch (NoSuchAlgorithmException e) {
			// ignored
			return null;
		}

	}

	public static String getShaUrlSafeString(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
			String fileName = Base64.encodeBase64URLSafeString(md.digest(text
					.getBytes()));
			return fileName;
		} catch (NoSuchAlgorithmException e) {
			// ignored
			return null;
		}

	}

	private static String byteHEX(byte[] ibs){
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
				'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer(ibs.length*2);
		for (int i = 0; i < 16; i++) {
			char ob0 = Digit[(ibs[i] >>> 4) & 0X0F];
			char ob1 = Digit[ibs[i] & 0X0F];
			buf.append(ob0);
			buf.append(ob1);
		}
		return buf.toString();
		
	}

	/**
	 * 返回MD5值
	 * @param text
	 * @return MD5值.所有字母为小写
	 */
	public static String getHexMD5(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(text.getBytes());
			return byteHEX(digest);
		} catch (NoSuchAlgorithmException e) {
			// ignored
			return null;
		}

	}
	/**
	 * 返回SHA值
	 * @param text
	 * @return MD5值.所有字母为小写
	 */
	public static String getHexSHA(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
			byte[] digest = md.digest(text.getBytes());
			return byteHEX(digest);
		} catch (NoSuchAlgorithmException e) {
			// ignored
			return null;
		}

	}
}
