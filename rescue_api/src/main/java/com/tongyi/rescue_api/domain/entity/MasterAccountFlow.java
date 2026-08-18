package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "master_account_flow",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_flow_master_biz_scene", columnNames = {"master_id", "biz_no", "scene"})
        },
        indexes = {
                @Index(name = "idx_flow_master_time", columnList = "master_id,create_time")
        }
)
public class MasterAccountFlow {

    @Id
    private String id;

    @Column(name = "master_id", nullable = false)
    private String masterId;

    /** 业务单号（提现单 applyNo） */
    @Column(name = "biz_no", nullable = false)
    private String bizNo;

    /** WITHDRAW_FREEZE / WITHDRAW_SUCCESS / WITHDRAW_ROLLBACK */
    @Column(name = "scene", nullable = false)
    private String scene;

    /** FREEZE / UNFREEZE / OUT */
    @Column(name = "change_type", nullable = false)
    private String changeType;

    /** 变动金额（分） */
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "frozen_before", nullable = false)
    private Integer frozenBefore;

    @Column(name = "frozen_after", nullable = false)
    private Integer frozenAfter;

    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    private Integer deleted;
}