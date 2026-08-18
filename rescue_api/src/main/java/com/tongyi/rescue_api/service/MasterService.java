package com.tongyi.rescue_api.service;

import com.tongyi.rescue_api.domain.entity.Master;

public interface MasterService {

    Master getMasterById(String id);

    Master getMasterByOpenId(String openId);

    Master getMasterByPhoneNumber(String phoneNumber);

    Master createMaster(Master master);

    Master updateMaster(Master master);

    /**
     * 师傅端微信登录逻辑：按 openId 幂等登录/注册，必要时绑定或更新手机号、昵称
     */
    Master wechatLogin(String openId, String unionId, String phoneNumber, String nickName);
}
