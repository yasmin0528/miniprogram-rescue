package com.tongyi.rescue_api.common.utils.wx;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class WxPayUtils {
    public static String buildAuthorizationURL(RequestMethod method, String urlSuffix, String mchId,
                                               String body, String keyPath, String certPath) throws Exception {
        String timestamp = timestamp();
        String nonceStr = nonceStr();
        String serialNo = RsaUtils.getSerialNoFromURL(certPath);
        // 构建签名参数
        String buildSignMessage = buildSignMessage(method, urlSuffix, timestamp, nonceStr, body);
        String signature = createSignURL(buildSignMessage, keyPath);
        log.info("微信支付接口：{}，请求参数：{}", urlSuffix, body);
        log.info("微信API证书序列号：{}", serialNo);
        // 根据平台规则生成请求头 authorization
        return getAuthorization(mchId, serialNo, nonceStr, timestamp, signature);
    }
    public static String buildAuthorizationURLGet(RequestMethod method, String urlSuffix, String mchId,
                                                  Map<String, Object> params, String keyPath, String certPath) throws Exception {
        String timestamp = timestamp();
        String nonceStr = nonceStr();
        String serialNo = RsaUtils.getSerialNoFromURL(certPath);

        // 1. 处理GET请求参数，拼接到urlSuffix中
        String finalUrlSuffix = urlSuffix;
        if (method == RequestMethod.GET && params != null && !params.isEmpty()) {
            String queryString = params.entrySet().stream()
                    .map(entry -> {
                        try {
                            // 对参数名和参数值进行URL编码
                            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString());
                            String encodedValue = URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.toString());
                            return encodedKey + "=" + encodedValue;
                        } catch (Exception e) {
                            // 处理编码异常
                            throw new RuntimeException("URL编码失败", e);
                        }
                    })
                    .collect(Collectors.joining("&"));
            finalUrlSuffix = urlSuffix + "?" + queryString;
        }

        // GET请求的body为空字符串
        String body = "";

        // 构建签名参数
        String buildSignMessage = buildSignMessage(method, finalUrlSuffix, timestamp, nonceStr, body);
        String signature = createSignURL(buildSignMessage, keyPath);
        log.info("微信支付接口：{}，请求参数：{}", finalUrlSuffix, body); // finalUrlSuffix现在包含了查询参数
        log.info("微信API证书序列号：{}", serialNo);
        // 根据平台规则生成请求头 authorization
        return getAuthorization(mchId, serialNo, nonceStr, timestamp, signature);
    }

    public static boolean verifySignatureFromURL(HttpResponse response, String certUrl) throws Exception {
        String timestamp = response.getHeader("Wechatpay-Timestamp");
        String nonceStr = response.getHeader("Wechatpay-Nonce");
        String signature = response.getHeader("Wechatpay-Signature");
        String body = response.getBody();

        URL url = new URL(certUrl);
        return verifySignature(signature, body, nonceStr, timestamp, url.openStream());
    }
    public static Map<String, String>
    jsApiCreateSign(String appId, String prepayId, String keyPath) throws Exception {
        String timeStamp = timestamp();
        String nonceStr = nonceStr();
        String packageStr = "prepay_id=" + prepayId;
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(packageStr);
        String packageSign = createSignURL(buildSignMessage(list), keyPath);
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", timeStamp);
        packageParams.put("nonceStr", nonceStr);
        packageParams.put("package", packageStr);
        packageParams.put("signType", "RSA");
        packageParams.put("paySign", packageSign);
        return packageParams;
    }
    public static Map<String, String> useNowPayLaterApiCreateSign(String appId, String prepayId,String apiV2) throws Exception {
        String timeStamp = timestamp();
        String nonceStr = nonceStr();
        String packageStr = prepayId;
        ArrayList<String> list = new ArrayList<>();
        list.add(appId);
        list.add(timeStamp);
        list.add(nonceStr);
        list.add(packageStr);
        Map<String, String> packageParams = new HashMap<>(6);
        packageParams.put("appId", appId);
        packageParams.put("timeStamp", timeStamp);
        packageParams.put("nonceStr", nonceStr);
        packageParams.put("package", packageStr);
        packageParams.put("signType", "HMAC-SHA256");
        String sign = WxPayUtil.generateSignature(packageParams, apiV2);
        packageParams.put("sign", sign);
        return packageParams;
    }

    /**
     * 验证签名
     *
     * @param signature       待验证的签名
     * @param body            应答主体
     * @param nonce           随机串
     * @param timestamp       时间戳
     * @param certInputStream 微信支付平台证书输入流
     * @return 签名结果
     * @throws Exception 异常信息
     */
    public static boolean verifySignature(String signature, String body, String nonce, String timestamp, InputStream certInputStream) throws Exception {
        String buildSignMessage = buildSignMessage(timestamp, nonce, body);
        // 获取证书
        X509Certificate certificate = RsaUtils.getCertificate(certInputStream);
        PublicKey publicKey = certificate.getPublicKey();
        return RsaUtils.checkByPublicKey(buildSignMessage, signature, publicKey);
    }

    /**
     * 验证签名（直接使用公钥）
     */
    public static boolean verifySignature(String signature, String body, String nonce, String timestamp, PublicKey publicKey) throws Exception {
        String buildSignMessage = buildSignMessage(timestamp, nonce, body);
        return RsaUtils.checkByPublicKey(buildSignMessage, signature, publicKey);
    }
    /**
     * 获取授权认证信息
     *
     * @param mchId     商户号
     * @param serialNo  商户API证书序列号
     * @param nonceStr  请求随机串
     * @param timestamp 时间戳
     * @param signature 签名值
     * @return 请求头 Authorization
     */
    public static String getAuthorization(String mchId, String serialNo, String nonceStr, String timestamp, String signature) {
        Map<String, String> params = new HashMap<>(5);
        params.put("mchid", mchId);
        params.put("serial_no", serialNo);
        params.put("nonce_str", nonceStr);
        params.put("timestamp", timestamp);
        params.put("signature", signature);
        String authorization = WxPayConstant.Attribute.AUTH_TYPE.concat(" ").concat(createLinkString(params, ",", false, true));
        log.info("微信生成的完整签名：{}", authorization);
        return authorization;
    }
    public static String createLinkString(Map<String, String> params, String connStr, boolean encode, boolean quotes) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            // 拼接时，不包括最后一个&字符
            if (i == keys.size() - 1) {
                if (quotes) {
                    content.append(key).append("=").append('"').append(encode ? urlEncode(value) : value).append('"');
                } else {
                    content.append(key).append("=").append(encode ? urlEncode(value) : value);
                }
            } else {
                if (quotes) {
                    content.append(key).append("=").append('"').append(encode ? urlEncode(value) : value).append('"').append(connStr);
                } else {
                    content.append(key).append("=").append(encode ? urlEncode(value) : value).append(connStr);
                }
            }
        }
        return content.toString();
    }
    /**
     * URL 编码
     *
     * @param src 需要编码的字符串
     * @return 编码后的字符串
     */
    public static String urlEncode(String src) {
        try {
            return URLEncoder.encode(src, CharsetUtil.UTF_8).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String createSignURL(String signMessage, String keyPath) throws Exception {
        if (StrUtil.isEmpty(signMessage)) {
            return null;
        }
        // 生成签名
        return RsaUtils.encryptByPrivateKeyFromURL(signMessage, keyPath);
    }
    /**
     * v3 接口创建签名
     *
     * @param signMessage 待签名的参数
     * @param keyPath     商户私钥证书路径
     * @return 生成 v3 签名
     * @throws Exception 异常信息
     */
    public static String createSign(String signMessage, String keyPath) throws Exception {
        if (StrUtil.isEmpty(signMessage)) {
            return null;
        }
        // 生成签名
        return RsaUtils.encryptByPrivateKey(signMessage, keyPath);
    }

    /**
     * 生成时间戳
     */
    private static String timestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 生成随机字符串
     */
    private static String nonceStr() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 构造签名串
     *
     * @param method    { RequestMethod} GET,POST,PUT等
     * @param url       可通过 {WxPayConstant.Api} 来获取，URL挂载参数需要自行拼接
     * @param timestamp 获取发起请求时的系统当前时间戳
     * @param nonceStr  随机字符串
     * @param body      请求报文主体
     * @return 待签名字符串
     */
    public static String buildSignMessage(RequestMethod method, String url, String timestamp, String nonceStr, String body) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(method.toString());
        arrayList.add(url);
        arrayList.add(timestamp);
        arrayList.add(nonceStr);
        arrayList.add(body);
        return buildSignMessage(arrayList);
    }

    /**
     * 构造签名串
     *
     * @param timestamp 应答时间戳
     * @param nonceStr  应答随机串
     * @param body      应答报文主体
     * @return 应答待签名字符串
     */
    public static String buildSignMessage(String timestamp, String nonceStr, String body) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(timestamp);
        arrayList.add(nonceStr);
        arrayList.add(body);
        return buildSignMessage(arrayList);
    }
    /**
     * 构造签名串
     *
     * @param signMessage 待签名的参数
     * @return 构造后带待签名串
     */
    public static String buildSignMessage(ArrayList<String> signMessage) {
        if (signMessage == null || signMessage.size() <= 0) {
            return null;
        }
        StringBuilder sbf = new StringBuilder();
        for (String str : signMessage) {
            sbf.append(str).append("\n");
        }
        String signStr = sbf.toString();
        log.info("微信构建的签名串：\n{}", signStr);
        return signStr;
    }
    /**
     * 获取商户私钥
     *
     * @param keyPath 商户私钥证书路径
     * @return {PrivateKey} 商户私钥
     * @throws Exception 异常信息
     */
    public static PrivateKey getPrivateKey(String keyPath) throws Exception {
        Resource resource = new ClassPathResource(keyPath);
        String originalKey = IoUtil.read(resource.getInputStream(), StandardCharsets.UTF_8);
        return getPrivateKeyByKeyContent(originalKey);
    }
    /**
     * 获取商户私钥
     *
     * @param originalKey 私钥文本内容
     * @return { PrivateKey} 商户私钥
     * @throws Exception 异常信息
     */
    public static PrivateKey getPrivateKeyByKeyContent(String originalKey) throws Exception {
        String privateKey = originalKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        return RsaUtils.loadPrivateKey(privateKey);
    }

    public static PrivateKey getPrivateKeyFromURL(String keyUrl) throws Exception {
        URL url = new URL(keyUrl);
        String originalKey = IoUtil.read(url.openStream(), StandardCharsets.UTF_8);
        return getPrivateKeyByKeyContent(originalKey);
    }
    public static String verifyNotifyFromURL(String serialNo, String body, String signature, String nonce,
                                             String timestamp, String key, String certUrl) throws Exception {
        URL url = new URL(certUrl);
        return verifyNotify(serialNo, body, signature, nonce, timestamp, key, url.openStream());
    }
    /**
     * v3 支付异步通知验证签名
     *
     * @param serialNo        证书序列号
     * @param body            异步通知密文
     * @param signature       签名
     * @param nonce           随机字符串
     * @param timestamp       时间戳
     * @param key             api 密钥
     * @param certInputStream 平台证书
     * @return 异步通知明文
     * @throws Exception 异常信息
     */
    public static String verifyNotify(String serialNo, String body, String signature, String nonce,
                                      String timestamp, String key, InputStream certInputStream) throws Exception {
        // 获取平台证书序列号
        X509Certificate certificate = RsaUtils.getCertificate(certInputStream);
        String serialNumber = certificate.getSerialNumber().toString(16).toUpperCase();
        // 验证证书序列号
        if (!serialNumber.equals(serialNo)) {
            throw new Exception("证书序列号错误");
        }

        boolean verified = verifySignature(signature, body, nonce, timestamp, certificate.getPublicKey());
        if (!verified) {
            throw new Exception("签名错误");
        }

        JSONObject resultObject = JSON.parseObject(body);
        JSONObject resource = resultObject.getJSONObject("resource");
        String cipherText = resource.getString("ciphertext");
        String nonceStr = resource.getString("nonce");
        String associatedData = resource.getString("associated_data");

        AesUtils aesUtil = new AesUtils(key.getBytes(StandardCharsets.UTF_8));
        // 密文解密
        return aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonceStr.getBytes(StandardCharsets.UTF_8),
                cipherText
        );
    }

}


