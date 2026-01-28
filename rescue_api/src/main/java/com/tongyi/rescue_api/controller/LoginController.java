package com.tongyi.rescue_api.controller;

import com.tongyi.rescue_api.common.Result;
import com.tongyi.rescue_api.domain.dto.LoginDTO;
import com.tongyi.rescue_api.domain.entity.User;
import com.tongyi.rescue_api.security.JwtUtil;
import com.tongyi.rescue_api.security.LoginUser;
import com.tongyi.rescue_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginUser> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.getUserByUsername(loginDTO.getUsername());
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error("密码错误");
        }
        
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), token);
        
        return Result.success(loginUser);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDTO loginDTO) {
        User existUser = userService.getUserByUsername(loginDTO.getUsername());
        
        if (existUser != null) {
            return Result.error("用户已存在");
        }
        
        User user = new User();
        user.setUsername(loginDTO.getUsername());
        user.setPassword(loginDTO.getPassword());
        user.setStatus("active");
        user.setCreatedTime(new java.util.Date());
        
        userService.createUser(user);
        return Result.success("注册成功");
    }
}
