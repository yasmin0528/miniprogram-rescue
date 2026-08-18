package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.entity.User;

public interface UserService {

    User getUserById(String id);

    User getUserByOpenId(String openId);

    User getUserByPhoneNumber(String phoneNumber);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(String id);

    /**
     * 微信登录：根据 openId / phoneNumber 查找或创建用户（隐式注册）。
     */
    User wechatLogin(String openId, String phoneNumber, String nickName);
}
