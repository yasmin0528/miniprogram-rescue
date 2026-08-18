package com.tongyi.rescue_api.common.utils.wx;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class WxPayUtil {

    /**
     * 生成微信支付签名
     *
     * @param params 参数Map，不包含key字段
     * @param apiKey 微信支付API密钥
     * @return 签名字符串
     */
    public static String generateSignature(Map<String, String> params, String apiKey) {
        // 1. 按参数名ASCII码排序
        Map<String, String> sortedMap = new TreeMap<>(params);

        // 2. 拼接成 key1=value1&key2=value2 的格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) { // 跳过空值
                sb.append(key).append("=").append(value).append("&");
            }
        }

        // 3. 在字符串末尾拼接API密钥
        sb.append("key=").append(apiKey);

        // 4. 计算HMAC-SHA256签名
        return hmacSha256(sb.toString(), apiKey).toUpperCase();
    }

    /**
     * 使用HMAC-SHA256计算签名
     *
     * @param data 数据字符串
     * @param key  密钥
     * @return 签名字符串
     */
    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
        }
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                result.append("0");
            }
            result.append(hex);
        }
        return result.toString();
    }
}


