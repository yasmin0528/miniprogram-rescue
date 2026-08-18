package com.tongyi.rescue_api.service.job;

import com.tongyi.rescue_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpireCloseJob {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void closeExpiredOrders() {
        try {
            orderService.closeExpiredOrders();
        } catch (Exception e) {
            log.error("[order-expire] job execution failed", e);
        }
    }
}
