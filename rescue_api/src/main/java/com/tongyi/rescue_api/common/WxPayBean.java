package com.tongyi.rescue_api.common;

import com.tongyi.rescue_api.domain.vo.PaySettingQuery;
import com.tongyi.rescue_api.service.PaySettingService;
import com.tongyi.rescue_api.common.utils.wx.WxPayUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Component
public class WxPayBean {

    private static final String PAY_TYPE_MINI_PROGRAM = "MINI_PROGRAM";

    private final PaySettingService paySettingService;


    public WxPayBean(PaySettingService paySettingService) {
        this.paySettingService = paySettingService;
    }

    private PaySettingQuery currentSetting(String appId) {
        return paySettingService.queryPaySetting(appId == null ? "" : appId, PAY_TYPE_MINI_PROGRAM);
    }

    public String buildAuthorization(String appId, RequestMethod method, String urlSuffix, String body) throws Exception {
        PaySettingQuery setting = currentSetting(appId);
        return WxPayUtils.buildAuthorizationURL(
                method,
                urlSuffix,
                setting.getMchId(),
                body,
                setting.getKeyPemPath(),
                setting.getCertPath()
        );
    }

    public String buildAuthorizationGet(String appId, RequestMethod method, String urlSuffix, Map<String, Object> params) throws Exception {
        PaySettingQuery setting = currentSetting(appId);
        return WxPayUtils.buildAuthorizationURLGet(
                method,
                urlSuffix,
                setting.getMchId(),
                params,
                setting.getKeyPemPath(),
                setting.getCertPath()
        );
    }

    public String currentCertPath(String appId) {
        return currentSetting(appId).getCertPath();
    }
}
