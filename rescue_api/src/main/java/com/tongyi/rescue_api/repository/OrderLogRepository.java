package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {
    List<OrderLog> findByOrderIdInOrderByOperateTimeDesc(List<String> orderIds);

    List<OrderLog> findByOrderIdOrderByOperateTimeAsc(String orderId);
}
