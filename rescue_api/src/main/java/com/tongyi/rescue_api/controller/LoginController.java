package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.common.Result;
import com.tongyi.rescue_api.domain.dto.WechatCode2SessionResponse;
import com.tongyi.rescue_api.domain.dto.WechatLoginDTO;
import com.tongyi.rescue_api.domain.entity.Master;
import com.tongyi.rescue_api.domain.entity.User;
import com.tongyi.rescue_api.security.JwtUtil;
import com.tongyi.rescue_api.security.LoginUser;
import com.tongyi.rescue_api.service.MasterService;
import com.tongyi.rescue_api.service.UserService;
import com.tongyi.rescue_api.service.WechatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MasterService masterService;

    @Autowired
    private UserService userService;

    @Autowired
    private WechatService wechatService;

    private String mask(String v) {
        if (!StringUtils.hasText(v)) {
            return "";
        }
        int left = Math.min(6, v.length());
        return v.substring(0, left) + "***";
    }

    /**
     * 微信快捷登录 / 首次登录隐式注册
     * 新版手机号流程：使用 getPhoneNumber 的 phoneCode 调微信接口换手机号。
     */
    @PostMapping("/wechat/login")
    public Result<LoginUser> wechatLogin(@RequestBody WechatLoginDTO loginDTO) {
        if (loginDTO == null) {
            return Result.error("请求参数不能为空");
        }

        String clientType = loginDTO.getClientType();
        if (!StringUtils.hasText(clientType)) {
            return Result.error("clientType不能为空");
        }

        String loginCode = loginDTO.getLoginCode();
        if (!StringUtils.hasText(loginCode)) {
            return Result.error("loginCode不能为空");
        }

        String phoneCode = loginDTO.getPhoneCode();

        log.info("[login-debug][backend] request clientType={}, loginCodePrefix={}, phoneCodePrefix={}, hasNickName={}, hasAvatarUrl={}",
                clientType,
                mask(loginCode),
                mask(phoneCode),
                StringUtils.hasText(loginDTO.getNickName()),
                StringUtils.hasText(loginDTO.getAvatarUrl()));

        WechatCode2SessionResponse sessionResponse = wechatService.code2Session(clientType, loginCode);
        log.info("[login-debug][backend] code2session result errcode={}, errmsg={}, openidPrefix={}",
                sessionResponse.getErrcode(),
                sessionResponse.getErrmsg(),
                mask(sessionResponse.getOpenid()));

        if (sessionResponse.getErrcode() != null && sessionResponse.getErrcode() != 0) {
            return Result.error("微信登录失败：" + sessionResponse.getErrmsg());
        }

        String openId = sessionResponse.getOpenid();
        if (!StringUtils.hasText(openId)) {
            return Result.error("微信登录失败：未获取到openId");
        }
        String unionId = sessionResponse.getUnionid();

        String phoneNumber = null;
        if (StringUtils.hasText(phoneCode)) {
            try {
                phoneNumber = wechatService.getPhoneNumberByCode(clientType, phoneCode);
                log.info("[login-debug][backend] phone resolved by phoneCode, phonePrefix={}", mask(phoneNumber));
            } catch (Exception e) {
                log.error("[login-debug][backend] phoneCode exchange fail clientType={}, phoneCodePrefix={}",
                        clientType, mask(phoneCode), e);
                return Result.error("获取手机号失败，请重试授权");
            }
        } else {
            log.warn("[login-debug][backend] phoneCode missing in request");
        }

        if ("shifu".equals(clientType) && !StringUtils.hasText(phoneNumber)) {
            return Result.error("请授权手机号后登录");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(clientType);
        loginUser.setOpenId(openId);

        if ("shifu".equals(clientType)) {
            Master master = masterService.wechatLogin(openId, unionId, phoneNumber, loginDTO.getNickName());
            String token = JwtUtil.generateToken(master.getId(), master.getPhoneNumber());

            loginUser.setUserId(master.getId());
            loginUser.setToken(token);
            loginUser.setNickName(master.getNickName());
            loginUser.setPhoneNumber(master.getPhoneNumber());
        } else {
            User user = userService.wechatLogin(openId, phoneNumber, loginDTO.getNickName());
            String token = JwtUtil.generateToken(user.getId(), user.getPhoneNumber());

            loginUser.setUserId(user.getId());
            loginUser.setToken(token);
            loginUser.setNickName(user.getNickName());
            loginUser.setPhoneNumber(user.getPhoneNumber());
        }

        log.info("[login-debug][backend] login success clientType={}, userId={}, phonePrefix={}",
                loginUser.getClientType(), loginUser.getUserId(), mask(loginUser.getPhoneNumber()));

        return Result.success(loginUser);
    }
}
