package com.tongyi.rescue_api.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayAppIdConfig {

    @Value("${pay.wechat.app-id.user:}")
    private String userAppId;

    @Value("${pay.wechat.app-id.shifu:}")
    private String shifuAppId;

    @PostConstruct
    public void logPayAppIds() {
        log.info("[pay-app-id] pay.wechat.app-id.user={}", userAppId);
        log.info("[pay-app-id] pay.wechat.app-id.shifu={}", shifuAppId);
    }

    public String getByClientType(String clientType) {
        if ("shifu".equalsIgnoreCase(clientType)) {
            return shifuAppId;
        }
        return userAppId;
    }
}