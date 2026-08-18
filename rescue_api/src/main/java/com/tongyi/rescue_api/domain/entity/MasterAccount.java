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
        name = "master_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_master_account_master_deleted", columnNames = {"master_id", "deleted"})
        },
        indexes = {
                @Index(name = "idx_master_account_master_deleted", columnList = "master_id,deleted")
        }
)
public class MasterAccount {

    @Id
    private String id;

    @Column(name = "master_id")
    private String masterId;

    /** 总余额（分） */
    @Column(name = "balance_amount")
    private Integer balanceAmount;

    /** 冻结金额（分） */
    @Column(name = "frozen_amount")
    private Integer frozenAmount;

    @Version
    private Integer version;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private Integer deleted;
}
