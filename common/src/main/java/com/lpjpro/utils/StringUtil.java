package com.lpjpro.utils;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    /**
     * 邮箱验证
     * @param email
     * @return
     */
    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 生成六位随机验证码
     * @return
     */
    public static String randomSixCode() {
        // 使用 SecureRandom 提高随机性
        SecureRandom random = new SecureRandom();
        int code = (int) ((random.nextDouble() * 9 + 1) * 100000);
        return String.format("%06d", code);
    }
}
