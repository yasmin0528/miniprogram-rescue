package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByDispatchToken(String dispatchToken);

    List<Order> findByCustomerPhoneOrderByCreateTimeDesc(String customerPhone);

    List<Order> findByOrderReceivingIdOrderByCreateTimeDesc(String orderReceivingId);

    List<Order> findAllByOrderByCreateTimeDesc();

    List<Order> findByStatusAndIsDeletedAndCreateTimeLessThanEqual(String status, Integer isDeleted, LocalDateTime createTime);

    @Modifying
    @Query("update Order o set o.status = :toStatus, o.updateTime = :updateTime, o.updateBy = :updateBy where o.id = :orderId and o.status = :fromStatus and o.isDeleted = 0")
    int updateStatusIfCurrent(@Param("orderId") String orderId,
                              @Param("fromStatus") String fromStatus,
                              @Param("toStatus") String toStatus,
                              @Param("updateTime") LocalDateTime updateTime,
                              @Param("updateBy") String updateBy);

    @Modifying
    @Query("update Order o set o.status = :toStatus, o.updateTime = :updateTime, o.updateBy = :updateBy where o.id = :orderId and o.status in :fromStatuses and o.isDeleted = 0")
    int updateStatusIfCurrentIn(@Param("orderId") String orderId,
                                @Param("fromStatuses") List<String> fromStatuses,
                                @Param("toStatus") String toStatus,
                                @Param("updateTime") LocalDateTime updateTime,
                                @Param("updateBy") String updateBy);
}
