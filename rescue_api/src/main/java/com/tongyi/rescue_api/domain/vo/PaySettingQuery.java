package com.tongyi.rescue_api.domain.vo;

import lombok.Data;

@Data
public class PaySettingQuery {
    /**
     * 公众号id
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * APIv3密钥
     */
    private String apiV3Key;
    /**
     * APIv2密钥
     */
    private String apiV2Key;
    /**
     * 支付通知回调地址
     */
    private String notifyUrl;

    /**
     * 退款回调地址
     */
    private String refundNotifyUrl;

    /**
     * 转账回调地址
     */
    private String transferNotifyUrl;

    /**
     * API 证书中的 key.pem
     */
    private String keyPemPath;

    /**
     * 商户序列号
     */
    private String serialNo;

    /**
     * 微信支付V3-url前缀
     */
    private String baseUrl;
    /**
     * 平台证书文件的路径
     */
    private String platformCertPath;
    /**
     * cert.pem文件的路径
     */
    private String certPath;

    /**
     * cert.p12文件的路径
     */
    private String certP12Path;
    /**
     * 商户支付分服务的唯一标识，由32位数字组成。支付分产品权限审核通过后，微信支付运营会向商户提供该ID。
     */
    private String serviceId;
}


