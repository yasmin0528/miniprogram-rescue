package com.tongyi.rescue_api.event;

import com.tongyi.rescue_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSucceededEventListener {

    private final OrderService orderService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || !StringUtils.hasText(event.bizOrderNo())) {
            log.warn("[pay-event] ignore invalid event, event={}", event);
            return;
        }
        try {
            orderService.handlePayCallback(event.bizOrderNo());
            log.info("[pay-event] callback success, bizOrderNo={}, outTradeNo={}", event.bizOrderNo(), event.outTradeNo());
        } catch (Exception e) {
            log.error("[pay-event] callback failed, bizOrderNo={}, outTradeNo={}", event.bizOrderNo(), event.outTradeNo(), e);
        }
    }
}
