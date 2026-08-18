package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.vo.PaySettingQuery;

public interface PaySettingService {
    PaySettingQuery queryPaySetting(String appId, String type);
}
