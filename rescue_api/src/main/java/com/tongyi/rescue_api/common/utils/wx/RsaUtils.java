package com.tongyi.rescue_api.common.utils.wx;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class RsaUtils {
    /**
     * 加密算法RSA
     */
    private static final String KEY_ALGORITHM = "RSA";
    public static String encryptByPrivateKeyFromURL(String data, String keyUrl) throws Exception {
        PrivateKey privateKey = WxPayUtils.getPrivateKeyFromURL(keyUrl);
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return StrUtil.str(Base64.encode(signed));
    }
    /**
     * key.pem 私钥签名
     *
     * @param data    需要加密的数据
     * @param keyPath 商户私钥证书路径
     * @return 加密后的数据
     * @throws Exception 异常信息
     */
    public static String encryptByPrivateKey(String data, String keyPath) throws Exception {
        PrivateKey privateKey = WxPayUtils.getPrivateKey(keyPath);
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signed = signature.sign();
        return StrUtil.str(Base64.encode(signed));
    }
    /**
     * 公钥验证签名
     *
     * @param data      需要加密的数据
     * @param sign      签名
     * @param publicKey 公钥
     * @return 验证结果
     * @throws Exception 异常信息
     */
    public static boolean checkByPublicKey(String data, String sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decode(sign.getBytes(StandardCharsets.UTF_8)));
    }
    /**
     * cert.pem 获取证书
     *
     * @param inputStream 证书文件
     * @return {@link X509Certificate} 获取证书
     */
    public static X509Certificate getCertificate(InputStream inputStream) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("证书已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("证书尚未生效", e);
        } catch (CertificateException e) {
            throw new RuntimeException("无效的证书", e);
        }
    }
    /**
     * cert.pem 获取商户序列号
     *
     * @param certPath cert.pem 证书路径
     */
    public static String getSerialNo(String certPath) throws IOException {
        Resource resource = new ClassPathResource(certPath);
        X509Certificate certificate = getCertificate(resource.getInputStream());
        return certificate.getSerialNumber().toString(16).toUpperCase();
    }

    public static String getSerialNoFromURL(String certUrl) throws IOException {
        URL url = new URL(certUrl);
        X509Certificate certificate = getCertificate(url.openStream());
        return certificate.getSerialNumber().toString(16).toUpperCase();
    }
    /**
     * key.pem 从字符串中加载私钥
     * <br>
     * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。
     *
     * @param privateKeyStr 私钥
     * @return {@link PrivateKey}
     * @throws Exception 异常信息
     */
    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            byte[] buffer = Base64.decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }
    /**
     * 计算 HMAC-SHA256
     *
     * @param data         输入数据
     * @param secretKeyStr 密钥
     * @return HMAC-SHA256 签名的结果
     * @throws Exception 异常
     */
    public static String computeHMACSHA256(String data, String secretKeyStr) throws Exception {
        try {
            // 1. 创建 SecretKeySpec 来包装密钥
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyStr.getBytes(), "HmacSHA256");

            // 2. 获取 Mac 实例并初始化
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            // 3. 计算 HMAC-SHA256
            byte[] hmacBytes = mac.doFinal(data.getBytes());

            // 4. 将字节数组转为 Base64 编码的字符串
            return java.util.Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new Exception("HMAC-SHA256 计算失败", e);
        }
    }
}


