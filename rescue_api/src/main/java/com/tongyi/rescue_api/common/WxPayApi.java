package com.tongyi.rescue_api.common;

import cn.hutool.http.ContentType;
import com.tongyi.rescue_api.common.utils.wx.HttpClientUtils;
import com.tongyi.rescue_api.common.utils.wx.HttpResponse;
import com.tongyi.rescue_api.common.utils.wx.RsaUtils;
import com.tongyi.rescue_api.domain.vo.PaySettingQuery;
import com.tongyi.rescue_api.service.PaySettingService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@Component
public class WxPayApi {

    private static final String PAY_TYPE_MINI_PROGRAM = "MINI_PROGRAM";

    private final WxPayBean wxPayBean;
    private final PaySettingService paySettingService;


    public WxPayApi(WxPayBean wxPayBean, PaySettingService paySettingService) {
        this.wxPayBean = wxPayBean;
        this.paySettingService = paySettingService;
    }

    private PaySettingQuery currentSetting(String appId) {
        return paySettingService.queryPaySetting(appId == null ? "" : appId, PAY_TYPE_MINI_PROGRAM);
    }

    /**
     * V3 接口统一执行入口
     */
    public HttpResponse v3(String appId, RequestMethod method, String urlPrefix, String urlSuffix, String body) throws Exception {
        String requestBody = body == null ? "" : body;
        String authorization = wxPayBean.buildAuthorization(appId, method, urlSuffix, requestBody);
        String serialNo = RsaUtils.getSerialNoFromURL(currentSetting(appId).getCertPath());

        if (method == RequestMethod.GET) {
            return get(urlPrefix.concat(urlSuffix), authorization, serialNo, null);
        } else if (method == RequestMethod.POST) {
            return post(urlPrefix.concat(urlSuffix), authorization, serialNo, requestBody);
        }
        return null;
    }

    public HttpResponse v3_1(String appId, RequestMethod method, String urlPrefix, String urlSuffix, Map<String, Object> params) throws Exception {
        String authorization = wxPayBean.buildAuthorizationGet(appId, method, urlSuffix, params);
        String serialNo = RsaUtils.getSerialNoFromURL(currentSetting(appId).getCertPath());

        if (method == RequestMethod.GET) {
            return get(urlPrefix.concat(urlSuffix), authorization, serialNo, params);
        }
        return null;
    }

    public HttpResponse get(String url, String authorization, String serialNo, Map<String, Object> params) {
        return HttpClientUtils.getDelegate().get(url, params, getHeaders(authorization, serialNo));
    }

    public HttpResponse post(String url, String authorization, String serialNumber, String data) {
        return HttpClientUtils.getDelegate().post(url, data, getHeaders(authorization, serialNumber));
    }

    public Map<String, String> getHeaders(String authorization, String serialNo) {
        Map<String, String> headers = getBaseHeaders(authorization);
        headers.put("Content-Type", ContentType.JSON.toString());
        if (StringUtils.hasText(serialNo)) {
            headers.put("Wechatpay-Serial", serialNo);
        }
        return headers;
    }

    private static final String OS = System.getProperty("os.name") + "/" + System.getProperty("os.version");
    private static final String VERSION = System.getProperty("java.version");

    public Map<String, String> getBaseHeaders(String authorization) {
        String userAgent = String.format(
                "WeChatPay-HttpClient/%s (%s) Java/%s",
                WxPayApi.class.getPackage().getImplementationVersion(),
                OS,
                VERSION == null ? "Unknown" : VERSION);

        Map<String, String> headers = new HashMap<>(5);
        headers.put("Accept", ContentType.JSON.toString());
        headers.put("Authorization", authorization);
        headers.put("User-Agent", userAgent);
        return headers;
    }
}
