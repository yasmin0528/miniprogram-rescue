package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rescue_order_log")
public class OrderLog {

    @Id
    private String id;

    /** 订单ID(rescue_order.id) */
    private String orderId;

    /** 状态值: CREATED/PAID/ACCEPTED/DEPARTED/ARRIVED/COMPLETED/CANCELLED */
    private String status;

    /** 状态发生时间 */
    private LocalDateTime operateTime;

    // 审计字段
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
