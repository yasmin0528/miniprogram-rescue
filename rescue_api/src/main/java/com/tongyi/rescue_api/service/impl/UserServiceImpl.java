package com.tongyi.rescue_api.service.impl;

import com.tongyi.rescue_api.domain.entity.User;
import com.tongyi.rescue_api.repository.UserRepository;
import com.tongyi.rescue_api.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import com.tongyi.rescue_api.common.IdWorker;

@Service
public class UserServiceImpl implements UserService {

    private static final int STATUS_ACTIVE = 1;
    private static final int NOT_DELETED = 0;

    private final UserRepository userRepository;
    private final IdWorker idWorker = new IdWorker();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        Optional<User> optional = userRepository.findById(id);
        User user = optional.orElse(null);
        if (user == null || user.getIsDeleted() == null || user.getIsDeleted() != NOT_DELETED) {
            return null;
        }
        return user;
    }

    @Override
    public User getUserByOpenId(String openId) {
        if (!StringUtils.hasText(openId)) {
            return null;
        }
        return userRepository.findByOpenIdAndIsDeleted(openId, NOT_DELETED).orElse(null);
    }

    @Override
    public User getUserByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        return userRepository.findByPhoneNumberAndIsDeleted(phoneNumber, NOT_DELETED).orElse(null);
    }

    @Override
    public User createUser(User user) {
        if (user == null) {
            return null;
        }
        if (!StringUtils.hasText(user.getId())) {
            user.setId(idWorker.nextIds());
        }
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreateTime() == null) {
            user.setCreateTime(now);
        }
        user.setUpdateTime(now);
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(NOT_DELETED);
        }
        if (user.getStatus() == null) {
            user.setStatus(STATUS_ACTIVE);
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        if (user == null || !StringUtils.hasText(user.getId())) {
            return null;
        }
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) {
        if (!StringUtils.hasText(id)) {
            return;
        }
        User user = getUserById(id);
        if (user == null) {
            return;
        }
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User wechatLogin(String openId, String phoneNumber, String nickName) {
        User user = getUserByOpenId(openId);

        if (user == null && StringUtils.hasText(phoneNumber)) {
            user = getUserByPhoneNumber(phoneNumber);
        }

        LocalDateTime now = LocalDateTime.now();

        if (user == null) {
            user = new User();
            user.setId(idWorker.nextIds());
            if (StringUtils.hasText(phoneNumber)) {
                user.setPhoneNumber(phoneNumber);
            }
            user.setOpenId(openId);
            user.setNickName(nickName);
            user.setStatus(STATUS_ACTIVE);
            user.setIsDeleted(NOT_DELETED);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user = userRepository.save(user);
        } else {
            boolean changed = false;
            if (StringUtils.hasText(openId) && !openId.equals(user.getOpenId())) {
                user.setOpenId(openId);
                changed = true;
            }
            if (StringUtils.hasText(phoneNumber) && !phoneNumber.equals(user.getPhoneNumber())) {
                user.setPhoneNumber(phoneNumber);
                changed = true;
            }
            if (StringUtils.hasText(nickName) && !nickName.equals(user.getNickName())) {
                user.setNickName(nickName);
                changed = true;
            }
            if (changed) {
                user.setUpdateTime(now);
                user = userRepository.save(user);
            }
        }

        return user;
    }
}
