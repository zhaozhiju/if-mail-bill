package net.umpay.mailbill.util.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesUtil {
	private static Logger log = LoggerFactory.getLogger(DesUtil.class);
    /**
     * 3DES解密
     * 
      * @param desKeyBytes
     *            DESKEY 3DES密钥
     * @param desEncTextString
     *            经过BASE64编码的3DES加密数据
     * @return 返回通过3DESKEY解密desEncTextString的数据
     * @throws MailBillException 
     */
     
	public static String decrypt3DES(byte[] desKeyBytes, String desEncTextString) throws MailBillException{
        StringBuffer strBuf = new StringBuffer();
        SecretKey desKey = new SecretKeySpec(desKeyBytes, "DESede");
        byte[] desEncTextBytes = Base64.base64ToByteArray(desEncTextString);
        //
        Cipher c4;
		try {
			c4 = Cipher.getInstance("DESede/ECB/PKCS5Padding");
	        // 根据密钥，对Cipher对象进行初始化,DECRYPT_MODE表示解密模式
	        c4.init(Cipher.DECRYPT_MODE, desKey);
	        // 解密
	        byte[] desDecTextBytes = c4.doFinal(desEncTextBytes);
	        strBuf.append(new String(desDecTextBytes, "utf-8"));
	        return strBuf.toString();
		} catch (NoSuchAlgorithmException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ENCRYPTION_FAILED_EXCEPTION_CODE, ErrorCodeContants.ENCRYPTION_FAILED_EXCEPTION.getMsg(), log);
		} catch (NoSuchPaddingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.CIPHER_PADDING_CODE, ErrorCodeContants.CIPHER_PADDING.getMsg(), log);
		} catch (InvalidKeyException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INVALID_KEYS_EXCEPTION_CODE, ErrorCodeContants.INVALID_KEYS_EXCEPTION.getMsg(), log);
		} catch (IllegalBlockSizeException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.PASSWORD_SIZE_EXCEPTION_CODE, ErrorCodeContants.PASSWORD_SIZE_EXCEPTION.getMsg(), log);
		} catch (BadPaddingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.PASSWORD_DECRYPT3DES_EXCEPTION_CODE, ErrorCodeContants.PASSWORD_DECRYPT3DES_EXCEPTION.getMsg(), log);
		} catch (UnsupportedEncodingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION_CODE, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION.getMsg(), log);
		}
     }

     /**
     * 3DES加密
     * 
     * @param desKeyBytes
     *            3DES key(24位长度)
     * @param plainText
     *            待加密字符串
     * @return 返回加密后的字符串 并用BASE64编码 BASE64(3DES(src))
     * @throws MailBillException 
     * @throws Exception
     */
     
	public static String encrypt3DES(byte[] desKeyBytes, String plainText) throws MailBillException {
        // 用对称密钥加密原文
        // 生成Cipher对象，指定其支持3DES算法
        Cipher c2;
		try {
			c2 = Cipher.getInstance("DESede/ECB/PKCS5Padding");
	        // 根据密钥，对Cipher对象进行初始化,ENCRYPT_MODE表示加密模式
	        SecretKey desKey = new SecretKeySpec(desKeyBytes, "DESede");
	        c2.init(Cipher.ENCRYPT_MODE, desKey);
	        byte[] srcBytes = plainText.getBytes("utf-8");
	        
	        // 加密
	        byte[] encBytes = c2.doFinal(srcBytes);
	        return Base64.byteArrayToBase64(encBytes);
		} catch (NoSuchAlgorithmException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ENCRYPTION_FAILED_EXCEPTION_CODE, ErrorCodeContants.ENCRYPTION_FAILED_EXCEPTION.getMsg(), log);
		} catch (NoSuchPaddingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.CIPHER_PADDING_CODE, ErrorCodeContants.CIPHER_PADDING.getMsg(), log);
		} catch (InvalidKeyException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.INVALID_KEYS_EXCEPTION_CODE, ErrorCodeContants.INVALID_KEYS_EXCEPTION.getMsg(), log);
		} catch (UnsupportedEncodingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION_CODE, ErrorCodeContants.UNSUPPORTED_ENCODING_EXCEPTION.getMsg(), log);
		} catch (IllegalBlockSizeException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.PASSWORD_SIZE_EXCEPTION_CODE, ErrorCodeContants.PASSWORD_SIZE_EXCEPTION.getMsg(), log);
		} catch (BadPaddingException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.PASSWORD_ENCRYPTION_EXCEPTION_CODE, ErrorCodeContants.PASSWORD_ENCRYPTION_EXCEPTION.getMsg(), log);
		}
     }
     
     
     public static void main(String[] args) {
    	 String passwordkey = "123456789009876543211234";
//    	 String passwordkey = Constants.PASSWORDKEY;
    	 try {
    		 byte[] bytes = passwordkey.getBytes();
			String encrypt3des = DesUtil.encrypt3DES(bytes,"ningkkkk");
			System.out.println(encrypt3des);
			
			String decrypt3des = DesUtil.decrypt3DES(bytes,encrypt3des);
			System.out.println(decrypt3des);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
