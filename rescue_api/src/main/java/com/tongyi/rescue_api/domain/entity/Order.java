package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rescue_order")
public class Order {

    @Id
    private String id;

    @Column(unique = true)
    private String orderNo;

    /** 订单类型: 1即刻单 2预约单 */
    private Integer orderType;

    /** 预约时间(预约单必填) */
    private LocalDateTime appointmentTime;

    // 下单用户信息
    private String customerId;
    private String customerName;
    private String customerPhone;

    /** 服务类型编码，如 emergency_charge/jump_start/... */
    private String serviceType;

    // 车辆与位置信息
    private String plateNo;
    private String address;
    private Double lat;
    private Double lng;
    private String remark;

    /** 服务商ID */
    private String agencyId;

    /** 接单人id */
    @Column(name = "order_receiving_id")
    private String orderReceivingId;

    /** 是否服务商订单: 0否 1是 */
    @Column(name = "agency_order_type")
    private Integer agencyOrderType;

    /** 订单金额 */
    @Column(name = "price", precision = 10, scale = 2)
    private java.math.BigDecimal price;

    /** 抽成 */
    @Column(name = "ratio", precision = 5, scale = 2)
    private java.math.BigDecimal ratio;

    /** 可分账总金额（分） */
    @Column(name = "net_price")
    private Integer netPrice;

    /** 师傅可得金额（分） */
    @Column(name = "master_income_amount")
    private Integer masterIncomeAmount;

    /** 服务商可得金额（分） */
    @Column(name = "provider_income_amount")
    private Integer providerIncomeAmount;

    /** 是否派单：0否 1是 */
    @Column(name = "is_dispatch")
    private Integer isDispatch;

    /** 派单接收师傅ID（唯一来源） */
    @Column(name = "dispatch_id")
    private String dispatchId;

    /** 派单安全 token */
    @Column(name = "dispatch_token")
    private String dispatchToken;

    /** 结算状态：UNSETTLED/SETTLED */
    @Column(name = "settlement_status")
    private String settlementStatus;

    /**
     * 当前状态:
     * CREATED/PAID/ACCEPTED/DEPARTED/ARRIVED/COMPLETED/CANCELLED/REFUND
     */
    private String status;

    // 审计字段
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
}
