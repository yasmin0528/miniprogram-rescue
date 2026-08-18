package com.tongyi.rescue_api.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JsApiPayDTO {

    /**
     * 我方订单号
     */

    private String number;

    /**
     * 支付金额
     */

    private BigDecimal payAmount;

    /**
     * 商品描述
     * 示例值：Image形象店-深圳腾大-QQ公仔
     */

    private String description="包来电救援";

    /**
     * 用户标识openId
     */
    private String openId;

    private String clientType;

}

