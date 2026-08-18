package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, String> {

    Optional<Master> findByOpenIdAndIsDeleted(String openId, Integer isDeleted);

    Optional<Master> findByPhoneNumberAndIsDeleted(String phoneNumber, Integer isDeleted);
}
