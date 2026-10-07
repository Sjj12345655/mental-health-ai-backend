package com.abc.convert;

import com.abc.DTO.command.UserRegisterCommand;
import com.abc.DTO.response.UserLoginResponseDTO;
import com.abc.entity.User;
import com.abc.enumClass.UserStatus;

import java.time.LocalDateTime;

public class UserConvert {
    //构建响应DTO,分为内外层，先写UserLoginResponseDTO中的UserDetailResponseDTO


    //User 实体 → UserDetailResponseDTO
    public static UserLoginResponseDTO.UserDetailResponseDTO buildUserDetailResponseDTO(User user) {
        return  UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .gender(user.getGender())
                .status(user.getStatus())
                .genderDisplayName(getGenderDisplayName(user.getGender())) //getGenderDisplayname是一个数字转文字的方法
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreated())
                .updatedAt(user.getUpdated())
                .build();
    }
    //外层
    //token + UserDetailResponseDTO → UserLoginResponseDTO
    public static UserLoginResponseDTO buildUserLoginResponseDTO(String token,UserLoginResponseDTO.UserDetailResponseDTO userDetailResponseDTO) {
        return UserLoginResponseDTO.builder()
                .token(token)
                .roleType(userDetailResponseDTO.getUserTypeDisplayName())
                .userInfo(userDetailResponseDTO)
                .build();
    }

    //注册命令转实体
    //`registerCommandToEntity` ：UserRegisterCommand → User 实体
    public static User registerCommandToEntity(UserRegisterCommand command, String encodePassword) {
        return User.builder()
                .username(command.getUsername())
                .password(encodePassword)
                .email(command.getEmail())
                .phone(command.getPhone())
                .nickname(command.getNickname())
                .gender(command.getGender())
                .userType(command.getUserType())
                .birthday(command.getBirthday())
                .status(UserStatus.NORMAL.getCode()) //注册新用户，默认把状态设置成【正常】
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();

    }




    /**
     * 获取性别显示名称
     * @param gender 性别代码
     * @return 性别显示名称
     */
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

}
