package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.common.ResponseData;
import com.tongyi.rescue_api.domain.dto.JsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.RefundPayDTO;
import com.tongyi.rescue_api.domain.dto.TransferAccountsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.WithdrawCreateDTO;

import java.util.Map;

public interface PayService {
    /**
     * 微信小程序支付下单，返回前端调起支付参数
     */
    ResponseData<?> jsApiPay(JsApiPayDTO dto);

    /**
     * 退款申请
     */
    ResponseData<?> refundPay(RefundPayDTO dto);

    /**
     * 创建提现申请单
     */
    ResponseData<?> createWithdraw(WithdrawCreateDTO dto);

    /**
     * 提现申请
     */
    ResponseData<?> transferApply(TransferAccountsApiPayDTO dto);

    /**
     * 订单支付状态查询
     */
    ResponseData<?> getOrderStatus(String bizOrderNo);

    /**
     * 支付回调处理
     */
    String handlePayNotify(Map<String, String> headers, String body);

    /**
     * 退款回调处理
     */
    String handleRefundNotify(Map<String, String> headers, String body);

    /**
     * 退款回调处理（测试环境可用，无验签）
     */
    String handleRefundNotifyWithoutVerify(String body);

    /**
     * 提现回调处理
     */
    String handleTransferNotify(Map<String, String> headers, String body);

    /**
     * 提现记录查询
     */
    ResponseData<?> getWithdrawRecord(String applyNo);

    /**
     * 提现对账列表（按技师）
     */
    ResponseData<?> listWithdrawRecords(String masterId);
}
