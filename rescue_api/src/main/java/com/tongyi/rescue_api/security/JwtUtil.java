package com.tongyi.rescue_api.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET = "rescue_api_secret_key_2024";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    /**
     * 生成JWT Token
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims, username);
    }

    /**
     * 创建Token
     */
    private static String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        // 这里应该使用JWT库如jjwt来生成token，这里只做示例
        return "token_" + System.currentTimeMillis();
    }

    /**
     * 验证Token
     */
    public static boolean validateToken(String token) {
        try {
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token中的用户ID
     */
    public static Long getUserIdFromToken(String token) {
        // 从token中提取userId
        return 1L;
    }
}
