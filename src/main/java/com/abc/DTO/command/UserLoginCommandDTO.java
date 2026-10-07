package com.abc.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginCommandDTO {
    @NotBlank(message = "用户名不能为空") //这个注解的前提是导入了 jakarta.validation.constraints 包依赖
    @Size(min = 2, max = 20, message = "用户名长度在2到20个字符之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6到20个字符之间")
    private String password;
}
