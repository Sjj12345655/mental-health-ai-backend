package com.abc.DTO.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

// 用户注册命令
@Data
public class UserRegisterCommand {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度在2到20个字符之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6到20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "密码只能由字母、数字和下划线组成")
    private String password;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 2, max = 20, message = "昵称长度在2到20个字符之间")
    private String nickname;

    private LocalDateTime birthday;
    private Integer Status;

    @NotBlank(message = "确认密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "确认密码只能由字母、数字和下划线组成")
    private String confirmPassword;
    private Integer gender;
    private Integer userType;

}
