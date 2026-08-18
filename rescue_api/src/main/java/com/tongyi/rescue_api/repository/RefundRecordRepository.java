package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.RefundRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRecordRepository extends JpaRepository<RefundRecord, String> {
    Optional<RefundRecord> findByRefundNoAndDeleted(String refundNo, Integer deleted);
}
