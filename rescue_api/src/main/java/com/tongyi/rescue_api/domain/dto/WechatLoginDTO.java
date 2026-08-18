package com.tongyi.rescue_api.domain.dto;

/**
 * 微信登录请求参数 DTO（统一驼峰命名）
 */
public class WechatLoginDTO {

    /**
     * wx.login 返回的 code（登录 code）
     */
    private String loginCode;

    /**
     * getPhoneNumber 返回的动态令牌 code（用于换取手机号）
     */
    private String phoneCode;

    /**
     * 用户昵称（可选）
     */
    private String nickName;

    /**
     * 用户头像地址（可选）
     */
    private String avatarUrl;

    /**
     * 客户端类型：user（用户端）/ shifu（师傅端）
     */
    private String clientType;

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
