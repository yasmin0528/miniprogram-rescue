package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "refund_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refund_record_refund_no", columnNames = {"refund_no"})
        },
        indexes = {
                @Index(name = "idx_refund_out_trade_deleted", columnList = "out_trade_no,deleted")
        }
)
public class RefundRecord {

    @Id
    private String id;

    @Column(name = "refund_no")
    private String refundNo;

    @Column(name = "biz_order_no")
    private String bizOrderNo;

    @Column(name = "out_trade_no")
    private String outTradeNo;

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "refund_amount")
    private Integer refundAmount;

    @Column(name = "total_amount")
    private Integer totalAmount;

    private String reason;

    @Column(name = "open_id")
    private String openId;

    @Column(name = "app_id")
    private String appId;

    /** APPLYING/SUCCESS/CLOSED/ABNORMAL */
    private String status;

    @Column(name = "apply_time")
    private LocalDateTime applyTime;

    @Column(name = "success_time")
    private LocalDateTime successTime;

    @Column(name = "fail_reason")
    private String failReason;

    @Column(name = "notify_raw")
    private String notifyRaw;

    @Column(name = "notify_time")
    private LocalDateTime notifyTime;

    @Column(name = "order_status_before_refund")
    private String orderStatusBeforeRefund;

    @Version
    private Integer version;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private Integer deleted;
}
