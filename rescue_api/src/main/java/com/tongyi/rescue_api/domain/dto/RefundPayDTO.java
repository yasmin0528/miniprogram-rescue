package com.tongyi.rescue_api.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundPayDTO {

    /**
     * 业务订单号（可选，优先通过outTradeNo反查）
     */
    private String bizOrderNo;

    /**
     * 商户支付订单号（out_trade_no）
     */
    private String outTradeNo;

    /**
     * 我方退款单号
     */
    private String outRefundNo;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 原订单金额
     */
    private BigDecimal payAmount;

    private String clientType;
}


