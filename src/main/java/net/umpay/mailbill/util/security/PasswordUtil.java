package net.umpay.mailbill.util.security;


import java.util.Random;

import org.apache.commons.lang.text.StrBuilder;

public final class PasswordUtil {
    private PasswordUtil() {
        // no instance
    }
    
    /**
     * 以随机数方式创建指定长度的密码。
     * 
     * @param length 密码目标长度
     * @return 纯数字密码
     */
    public static String generateRandomPassword(int length) {
        Random random = new Random();
        String digits;
        if (length <= 9) {
            digits = String.valueOf(random.nextInt((int) Math.pow(10,length)));
        } else {
            digits = String.valueOf(Math.abs(random.nextLong()));
        }
        
        return new StrBuilder().
                appendFixedWidthPadLeft(digits, length, '0').
                toString();
    }
}
