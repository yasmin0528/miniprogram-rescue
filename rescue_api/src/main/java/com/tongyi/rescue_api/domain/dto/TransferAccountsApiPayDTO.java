package com.tongyi.rescue_api.domain.dto;

import lombok.Data;

@Data
public class TransferAccountsApiPayDTO {

    /**
     * 提现申请单号（由前端传入）
     */
    private String applyNo;
}
