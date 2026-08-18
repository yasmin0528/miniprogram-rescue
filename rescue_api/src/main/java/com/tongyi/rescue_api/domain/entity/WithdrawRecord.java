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
        name = "withdraw_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_withdraw_record_apply_no", columnNames = {"apply_no"}),
                @UniqueConstraint(name = "uk_withdraw_record_request_id_deleted", columnNames = {"request_id", "deleted"}),
                @UniqueConstraint(name = "uk_withdraw_record_out_bill_no_deleted", columnNames = {"out_bill_no", "deleted"})
        },
        indexes = {
                @Index(name = "idx_withdraw_master_deleted", columnList = "master_id,deleted"),
                @Index(name = "idx_withdraw_status_deleted", columnList = "status,deleted")
        }
)
public class WithdrawRecord {

    @Id
    private String id;

    @Column(name = "apply_no")
    private String applyNo;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "biz_order_no")
    private String bizOrderNo;

    @Column(name = "master_id")
    private String masterId;

    @Column(name = "open_id")
    private String openId;

    @Column(name = "transfer_bill_no")
    private String transferBillNo;

    @Column(name = "out_bill_no")
    private String outBillNo;

    @Column(name = "transfer_amount")
    private Integer transferAmount;

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

    @Column(name = "app_id")
    private String appId;

    @Version
    private Integer version;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private Integer deleted;
}
