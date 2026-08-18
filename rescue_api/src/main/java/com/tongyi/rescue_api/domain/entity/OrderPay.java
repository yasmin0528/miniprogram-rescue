package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_pay")
public class OrderPay {
    @Id
    private String id;

    @Column(name = "biz_order_no")
    private String bizOrderNo;

    @Column(name = "out_trade_no")
    private String outTradeNo;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "open_id")
    private String openId;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "mch_id")
    private String mchId;

    private String description;

    @Column(name = "total_amount")
    private Integer totalAmount;

    private String status;

    @Column(name = "time_expire")
    private LocalDateTime timeExpire;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "notify_raw")
    private String notifyRaw;

    @Column(name = "notify_time")
    private LocalDateTime notifyTime;

    @Column(name = "fail_reason")
    private String failReason;

    private Integer version;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private Integer deleted;
}
