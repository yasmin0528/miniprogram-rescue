package com.tongyi.rescue_api.common.utils.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesEncryptionUtil {
    public static final String key = "WatchingOverYous";

    // 加密
    public static String encrypt(String plaintext) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 解密
    public static String decrypt(String ciphertext) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    public static void main(String[] args) throws Exception {
        String appid = encrypt("wxd04df113070f65d9"); //wxd04df113070f65d9 wx8ca570d5497608a7
        String mchId = encrypt("1714468659");
        String apiV3Key = encrypt("7aB9Xy2R5pL8vE1qW3zT6mN4cK0jHdFg");
        String apiV2Key = encrypt("7aB9Xy2R5pL8vE1qW3zT6mN4cK0jHdFg");
        String baseUrl = encrypt("https://api.mch.weixin.qq.com/v3");
        String notifyUrl = encrypt("http://127.0.0.1/pay/payNotify"); //https://agreeing-alongside-cranberry.ngrok-free.dev/api/pay/notify/refund
        String refundNotifyUrl = encrypt("http://127.0.0.1/pay/refundNotify");
        String keyPemPath = encrypt("b_apiclient_key.pem");
        String serialNo = encrypt("3BF89E6060CD23420196501ACBD8BA116523D062");
        String platformCertPath = encrypt("platform_certificate.pem");
        String certPath = encrypt("b_apiclient_cert.pem");
        String certP12Path = encrypt("b_apiclient_cert.p12");
        String serviceId = encrypt("00003004000000173226501602215470");
        System.out.println("appid:"+appid);
        System.out.println("mchId:"+mchId);
        System.out.println("apiV3Key:"+apiV3Key);
        System.out.println("apiV2Key:"+apiV2Key);
        System.out.println("baseUrl:"+baseUrl);
        System.out.println("notifyUrl:"+notifyUrl);
        System.out.println("refundNotifyUrl:"+refundNotifyUrl);
        System.out.println("keyPemPath:"+keyPemPath);
        System.out.println("serialNo:"+serialNo);
        System.out.println("platformCertPath:"+platformCertPath);
        System.out.println("certPath:"+certPath);
        System.out.println("certP12Path:"+certP12Path);
        System.out.println("serviceId:"+serviceId);
        String decrypt = decrypt("8JJyKkJceaRPbkxDoHf18Q==");
        System.out.println(decrypt);
    }
}


