package com.abc.controller;

import com.abc.DTO.command.UserRegisterCommand;
import com.abc.DTO.response.UserLoginResponseDTO;
import com.abc.common.Result;
import com.abc.DTO.command.UserLoginCommandDTO;
import com.abc.service.UserService;
import com.abc.until.JwtTokenUntil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    //用户登录接口
    // @Valid` 是 **Java 参数校验（JSR303 规范）** 的注解，配合 `spring-boot-starter-validation` 依赖使用，
    // **自动校验对象里面字段的规则**，不用自己手写一堆 `if` 判断。
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        UserLoginResponseDTO userLoginResponseDTO = userService.login(userLoginCommandDTO);
        return Result.success(userLoginResponseDTO);
    }

    //用户注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommand userRegisterCommand) {
        UserLoginResponseDTO.UserDetailResponseDTO userDetailResponseDTO = userService.register(userRegisterCommand);
        return Result.success(userDetailResponseDTO);
    }

    //获取当前用户
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        //如何从token中获取当前用户id，需要使用JWT工具类解析token
        String token = JwtTokenUntil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUntil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        //根据用户id获取用户信息
        UserLoginResponseDTO.UserDetailResponseDTO userDetailResponseDTO = userService.getUserById(userId);
        return Result.success(userDetailResponseDTO);
    }
}
