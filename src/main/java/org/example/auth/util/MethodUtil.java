package org.example.auth.util;

import java.security.SecureRandom;

/**
 * @author fasonghao
 */
public class MethodUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 随机生成一段字符串
     * @return
     */
    public static String generateUsername(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
