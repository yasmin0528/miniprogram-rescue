package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.common.utils.aes.AesEncryptionUtil;
import com.tongyi.rescue_api.config.oss.OssTemplate;
import com.tongyi.rescue_api.domain.entity.PaySetting;
import com.tongyi.rescue_api.domain.vo.PaySettingQuery;
import com.tongyi.rescue_api.repository.PaySettingRepository;
import com.tongyi.rescue_api.service.PaySettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaySettingServiceImpl implements PaySettingService {
    final OssTemplate ossTemplate;
    private final PaySettingRepository paySettingRepository;

    @Override
    public PaySettingQuery queryPaySetting(String appId, String type) {
        log.info("[pay-setting] query start, appId={}, type={}, expectedIsStartUsing=1, expectedDeleted=0", appId, type);
        PaySetting setting = paySettingRepository
                .findFirstByAppIdAndTypeAndIsStartUsingAndDeleted(appId, type, 1, 0)
                .orElseThrow(() -> {
                    log.error("[pay-setting] query miss, appId={}, type={}, reason=not found or not enabled", appId, type);
                    return new IllegalStateException("支付配置不存在或未启用");
                });
        log.info("[pay-setting] query hit, id={}, appId={}, type={}, isStartUsing={}, deleted={}",
                setting.getId(), setting.getAppId(), setting.getType(), setting.getIsStartUsing(), setting.getDeleted());

        PaySettingQuery query = new PaySettingQuery();
        query.setAppId(setting.getAppId());
        query.setMchId(decryptIfNeeded(setting.getMchId()));
        query.setApiV3Key(decryptIfNeeded(setting.getApiV3Key()));
        query.setApiV2Key(decryptIfNeeded(setting.getApiV2Key()));
        query.setNotifyUrl(decryptIfNeeded(setting.getNotifyUrl()));
        query.setRefundNotifyUrl(decryptIfNeeded(setting.getRefundNotifyUrl()));
        query.setTransferNotifyUrl(decryptIfNeeded(setting.getTransferNotifyUrl()));
        query.setKeyPemPath(ossTemplate.getPermanentFileUrl(decryptIfNeeded(setting.getKeyPemPath())));
        query.setSerialNo(ossTemplate.getPermanentFileUrl(decryptIfNeeded(setting.getSerialNo())));
        query.setBaseUrl(decryptIfNeeded(setting.getBaseUrl()));
        query.setPlatformCertPath(ossTemplate.getPermanentFileUrl(decryptIfNeeded(setting.getPlatformCertPath())));
        query.setCertPath(ossTemplate.getPermanentFileUrl(decryptIfNeeded(setting.getCertPath())));
        query.setCertP12Path(ossTemplate.getPermanentFileUrl(decryptIfNeeded(setting.getCertP12Path())));
        query.setServiceId(decryptIfNeeded(setting.getServiceId()));
        return query;
    }

    private String decryptIfNeeded(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return AesEncryptionUtil.decrypt(value);
        } catch (Exception e) {
            log.debug("pay_setting field decrypt skipped, use raw value");
            return value;
        }
    }
}
