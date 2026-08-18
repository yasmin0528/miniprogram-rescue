package com.tongyi.rescue_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tongyi.rescue_api.domain.entity.PaySetting;

import java.util.Optional;

public interface PaySettingRepository extends JpaRepository<PaySetting, String> {
    Optional<PaySetting> findFirstByAppIdAndTypeAndIsStartUsingAndDeleted(String appId, String type, Integer isStartUsing, Integer deleted);
}
