package com.tongyi.rescue_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agencyprice")
public class AgencyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agencyid", length = 50, nullable = false)
    private String agencyid;

    @Column(name = "companyname", length = 255)
    private String companyname;

    @Column(name = "service", length = 255, nullable = false)
    private String service;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "ratio", precision = 5, scale = 2)
    private BigDecimal ratio;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "createtime")
    private LocalDateTime createtime;

    @Column(name = "updatetime")
    private LocalDateTime updatetime;
}
