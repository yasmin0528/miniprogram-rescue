package com.tongyi.rescue_api.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateDTO {
    /** 服务商ID */
    private String agencyId;

    private String customerId;
    private String customerName;
    private String customerPhone;

    /** 服务类型编码，如 tow/jump_start */
    private String serviceType;

    /** 订单类型：1即刻单，2预约单 */
    private Integer orderType;

    /** 预约时间（预约单可传） */
    private LocalDateTime appointmentTime;

    private String address;
    private Double lat;
    private Double lng;
    private String plateNo;
    private String remark;

    /** 是否服务商订单: 0否 1是 */
    private Integer agencyOrderType;

    /** 订单金额 */
    private java.math.BigDecimal price;

    /** 抽成 */
    private java.math.BigDecimal ratio;
}
