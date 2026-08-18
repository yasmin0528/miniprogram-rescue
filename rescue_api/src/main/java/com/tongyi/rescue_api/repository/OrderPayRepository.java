package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.OrderPay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderPayRepository extends JpaRepository<OrderPay, String> {
    Optional<OrderPay> findByOutTradeNoAndDeleted(String outTradeNo, Integer deleted);

    Optional<OrderPay> findTopByBizOrderNoAndDeletedOrderByCreateTimeDesc(String bizOrderNo, Integer deleted);

    List<OrderPay> findByStatusAndTimeExpireBeforeAndDeleted(String status, LocalDateTime timeExpire, Integer deleted);
}
