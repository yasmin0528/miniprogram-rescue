package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sys_master",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sys_master_open_id_deleted", columnNames = {"open_id", "is_deleted"}),
                @UniqueConstraint(name = "uk_sys_master_phone_deleted", columnNames = {"phone_number", "is_deleted"})
        },
        indexes = {
                @Index(name = "idx_sys_master_status", columnList = "status"),
                @Index(name = "idx_sys_master_is_deleted", columnList = "is_deleted")
        }
)
public class Master {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "open_id", length = 255, nullable = false)
    private String openId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "nick_name", length = 255)
    private String nickName;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "union_id", length = 255)
    private String unionId;

    public Master() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
}
