package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pay_setting")
public class PaySetting {
    @Id
    private String id;

    @Column(name = "app_id")
    private String appId;
    @Column(name = "mch_id")
    private String mchId;
    private String apiV3Key;
    @Column(name = "api_v2_key")
    private String apiV2Key;
    @Column(name = "notify_url")
    private String notifyUrl;
    @Column(name = "refund_notify_url")
    private String refundNotifyUrl;
    @Column(name = "key_pem_path")
    private String keyPemPath;
    @Column(name = "serial_no")
    private String serialNo;
    @Column(name = "base_url")
    private String baseUrl;
    @Column(name = "platform_cert_path")
    private String platformCertPath;
    @Column(name = "cert_path")
    private String certPath;
    @Column(name = "cert_p12_path")
    private String certP12Path;
    @Column(name = "is_start_using")
    private Integer isStartUsing;
    private String name;
    private String type;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "transfer_notify_url")
    private String transferNotifyUrl;
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    private Integer deleted;
}
