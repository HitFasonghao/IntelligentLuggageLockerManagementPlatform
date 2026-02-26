package org.example.auth.util;

import org.example.auth.constants.Constants;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * pc端Token生成工具类
 * @author fasonghao
 */
public class TokenUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成通用随机Token
     * @return 64位十六进制字符串（32字节转16进制后长度翻倍）
     */
    public static String generateRandomToken() {
        byte[] randomBytes = new byte[Constants.TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);

        //转十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 简化版：用UUID生成Token
     * @return 32位UUID字符串（去掉横线）
     */
    public static String generateUuidToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}