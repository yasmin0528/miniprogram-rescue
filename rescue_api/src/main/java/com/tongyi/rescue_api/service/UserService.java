package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.entity.User;
import com.tongyi.rescue_api.domain.vo.UserVO;

public interface UserService {
    User getUserById(Long id);
    User getUserByUsername(String username);
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    UserVO getUserVO(Long id);
}
