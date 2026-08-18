package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.MasterAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterAccountRepository extends JpaRepository<MasterAccount, String> {

    Optional<MasterAccount> findByMasterIdAndDeleted(String masterId, Integer deleted);
}
