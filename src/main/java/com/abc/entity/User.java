package com.abc.entity;

import com.abc.enumClass.UserStatus;
import com.abc.enumClass.UserType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import net.sf.jsqlparser.statement.upsert.UpsertType;

import java.time.LocalDateTime;

@Data
@TableName("user") //指定使用user表
@Builder
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度在2到20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能由字母、数字和下划线组成")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Size(min = 2, max = 50, message = "邮箱长度在2到50个字符之间")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6到20个字符之间")
    private String password;

    @Size(min = 2, max = 20, message = "昵称长度在2到20个字符之间")
    private String nickname;

    @Size(max = 225, message = "头像长度在225个字符内")
    private String avatar;

    private Integer gender;

    private LocalDateTime birthday;

    @TableField("user_type")
    private Integer userType;

    private Integer status;

    @TableField("created_at")
    private LocalDateTime created;

    @TableField("updated_at")
    private LocalDateTime updated;

    /**
     * 是否是普通用户
     */
//    public boolean isUser() {
//        return UserType.NORMAL.getCode().equals(this.userType);
//    }

    /**
     * 是否为正常状态
     */
    public boolean isActive() {
        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    /**
     * 获取用户类型显示名称
     */
    public String getUserTypeDisplayName() {
        try {
            return UserType.fromCode(userType).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    /**
     * 获取用户状态显示名称
     */
    public String getStatusDisplayName() {
        try {
            return UserStatus.fromCode(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }

    /**
     * 获取显示名称（优先显示昵称，否则显示用户名）
     */
    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }

    /**
     * 验证用户类型代码是否有效
     */
    public static boolean isValidCode(Integer code) {
        for (UserType type : UserType.values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }



}
