package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.AgencyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgencyPriceRepository extends JpaRepository<AgencyPrice, Long> {
    List<AgencyPrice> findByAgencyidAndStatusOrderByIdAsc(String agencyid, Integer status);
}
