package com.tongyi.rescue_api.repository;

import com.tongyi.rescue_api.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByOpenIdAndIsDeleted(String openId, Integer isDeleted);

    Optional<User> findByPhoneNumberAndIsDeleted(String phoneNumber, Integer isDeleted);
}
