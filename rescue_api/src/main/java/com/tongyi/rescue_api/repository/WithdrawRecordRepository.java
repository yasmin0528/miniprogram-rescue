package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.WithdrawRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WithdrawRecordRepository extends JpaRepository<WithdrawRecord, String> {

    Optional<WithdrawRecord> findByApplyNoAndDeleted(String applyNo, Integer deleted);

    Optional<WithdrawRecord> findByRequestIdAndDeleted(String requestId, Integer deleted);

    Optional<WithdrawRecord> findByOutBillNoAndDeleted(String outBillNo, Integer deleted);

    List<WithdrawRecord> findByMasterIdAndDeletedOrderByCreateTimeDesc(String masterId, Integer deleted);

    List<WithdrawRecord> findTop100ByStatusAndDeletedAndApplyTimeBeforeOrderByApplyTimeAsc(String status, Integer deleted, LocalDateTime applyTime);
}
