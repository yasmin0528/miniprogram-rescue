package com.tongyi.rescue_api.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawCreateDTO {

    /** 技师ID */
    private String masterId;

    /** 业务订单号（可选） */
    private String bizOrderNo;

    /** 到账账户ID（可选，当前仅透传保存扩展用） */
    private String accountId;

    /** 幂等请求ID */
    private String requestId;

    /** 提现金额（元） */
    private BigDecimal amountYuan;

    /** 提现金额（分），与amountYuan二选一，优先amountFen */
    private Integer amountFen;
}
