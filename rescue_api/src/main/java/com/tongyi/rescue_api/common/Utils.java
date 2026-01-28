package com.tongyi.rescue_api.common;

import java.util.UUID;

public class Utils {
    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + generateRandomCode(4);
    }

    /**
     * 生成随机码
     */
    public static String generateRandomCode(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
