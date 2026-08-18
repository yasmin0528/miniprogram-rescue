package com.tongyi.rescue_api.event;

public record PaymentSucceededEvent(String bizOrderNo, String outTradeNo) {
}
