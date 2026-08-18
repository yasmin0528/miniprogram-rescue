package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.common.ResponseData;
import com.tongyi.rescue_api.domain.dto.JsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.RefundPayDTO;
import com.tongyi.rescue_api.domain.dto.TransferAccountsApiPayDTO;
import com.tongyi.rescue_api.domain.dto.WithdrawCreateDTO;
import com.tongyi.rescue_api.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/jsapi/create")
    public ResponseData<?> createJsapiOrder(@RequestBody JsApiPayDTO dto) {
        return payService.jsApiPay(dto);
    }

    @PostMapping("/refund/apply")
    public ResponseData<?> applyRefund(@RequestBody RefundPayDTO dto) {
        return payService.refundPay(dto);
    }

    @PostMapping("/withdraw/create")
    public ResponseData<?> createWithdraw(@RequestBody WithdrawCreateDTO dto) {
        return payService.createWithdraw(dto);
    }

    @PostMapping("/withdraw/apply")
    public ResponseData<?> applyWithdraw(@RequestBody TransferAccountsApiPayDTO dto) {
        return payService.transferApply(dto);
    }

    @GetMapping("/order/{bizOrderNo}/status")
    public ResponseData<?> getOrderStatus(@PathVariable String bizOrderNo) {
        return payService.getOrderStatus(bizOrderNo);
    }

    @GetMapping("/withdraw/record")
    public ResponseData<?> getWithdrawRecord(@RequestParam String applyNo) {
        return payService.getWithdrawRecord(applyNo);
    }

    @GetMapping("/withdraw/reconcile")
    public ResponseData<?> listWithdrawRecords(@RequestParam String masterId) {
        return payService.listWithdrawRecords(masterId);
    }
}
