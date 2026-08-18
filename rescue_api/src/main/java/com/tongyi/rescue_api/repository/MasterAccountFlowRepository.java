package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.MasterAccountFlow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterAccountFlowRepository extends JpaRepository<MasterAccountFlow, String> {

    boolean existsByMasterIdAndBizNoAndSceneAndDeleted(String masterId, String bizNo, String scene, Integer deleted);
}
