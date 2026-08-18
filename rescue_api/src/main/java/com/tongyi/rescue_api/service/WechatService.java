package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.dto.WechatCode2SessionResponse;

public interface WechatService {

    /**
     * 调用微信 auth.code2Session 接口，通过登录 code 换取 openid 和 session_key。
     */
    WechatCode2SessionResponse code2Session(String clientType, String loginCode);

    /**
     * 调用微信手机号接口，通过 getPhoneNumber 的动态令牌 code 换取手机号。
     */
    String getPhoneNumberByCode(String clientType, String phoneCode);
}
