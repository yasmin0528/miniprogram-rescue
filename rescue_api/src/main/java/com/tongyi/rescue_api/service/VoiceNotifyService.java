package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.entity.Order;

public interface VoiceNotifyService {

    /**
     * 下单成功后通知服务商（语音专线）
     */
    void notifyAgencyOnOrderCreated(Order order);
}
