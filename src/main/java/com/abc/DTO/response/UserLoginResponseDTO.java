package com.abc.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserLoginResponseDTO {
    private String token;
    // 角色类型(管理员、用户等)
    private String roleType;
    // 用户信息
    private UserDetailResponseDTO userInfo;


    @Data
    @Builder
    public static class UserDetailResponseDTO {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String phone;
        private Integer gender;
        private String genderDisplayName;
        private LocalDateTime birthday;
        private Integer userType;
        // 用户类型显示名称(管理员、用户等)
        private String userTypeDisplayName;
        private Integer status;
        private String statusDisplayName;
        private String displayName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }
}
