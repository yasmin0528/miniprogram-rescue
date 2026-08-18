package com.tongyi.rescue_api.common.utils.wx;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean; // 用于控制初始化状态

public class WeChatPayEncryptorService {

    private PublicKey weChatPayPublicKey;
    private final String certHttpsUrl; // 证书URL，可以在构造函数中传入
    private final AtomicBoolean initialized = new AtomicBoolean(false); // 标记是否已初始化

    static {
        // 注册 Bouncy Castle Provider，这只需要一次，所以放在静态块
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 构造函数。在DI框架中，这个构造函数会被调用一次来创建单例实例。
     *
     * @param certHttpsUrl 微信支付平台证书的HTTPS下载地址
     */
    public WeChatPayEncryptorService(String certHttpsUrl) {
        Objects.requireNonNull(certHttpsUrl, "证书HTTPS URL不能为空");
        this.certHttpsUrl = certHttpsUrl;
        // 注意：在这里不直接调用 initializePublicKey()
        // 因为构造函数不应该执行耗时或可能失败的I/O操作
        // 初始化逻辑放在一个独立的 init 方法中，或者在第一次加密时触发
    }

    /**
     * 初始化方法。应该在组件被创建后（例如，在Spring的 @PostConstruct 或 Bean初始化回调）调用一次。
     * 负责从URL下载证书并加载公钥。
     * 这个方法应该是线程安全的，只执行一次。
     *
     * @throws Exception 如果证书下载失败或公钥加载失败
     */
    public void initializePublicKey() throws Exception {

    }

    /**
     * 使用缓存的微信支付平台公钥加密敏感信息。
     *
     * @param message 待加密的收款用户姓名
     * @return 加密后的Base64编码字符串
     * @throws Exception 如果公钥未初始化或加密失败
     */
    public  String encryptUserName(String message)
            throws IllegalBlockSizeException, IOException { // 这里修改了 throws IOException
        try {
            URL url = new URL(certHttpsUrl);
            X509Certificate certificate = RsaUtils.getCertificate(url.openStream());
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
            byte[] data = message.getBytes(StandardCharsets.UTF_8); // 使用StandardCharsets.UTF_8
            byte[] cipherdata = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(cipherdata);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的公钥", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            // 微信支付通常要求加密原文不能过长，否则会抛出此异常
            throw new IllegalBlockSizeException("加密原串的长度不能超过214字节");
        }
    }





}