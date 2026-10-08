package com.abc.service;

import cn.hutool.json.JSONUtil;
import com.abc.DTO.command.UserRegisterCommand;
import com.abc.common.Result;
import com.abc.DTO.command.UserLoginCommandDTO;
import com.abc.DTO.response.UserLoginResponseDTO;
import com.abc.convert.UserConvert;
import com.abc.entity.User;
import com.abc.exception.BusinessException;
import com.abc.mapper.UserMapper;
import com.abc.until.JwtTokenUntil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    //SpringSecurity 自带的密码加密工具类，用的是 BCrypt 加密算法。
    private final BCryptPasswordEncoder PasswordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * @param userLoginCommandDTO
     * @return
     */
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        //构建查询条件
        //`LambdaQueryWrapper` = **安全的 SQL 条件拼装工具**，不用写 xml，不用写字符串字段，用来组装查询条件，交给 Mapper 去查数据库。
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //只按"用户名或邮箱"查出用户。注意：不能把密码当查询条件——数据库存的是BCrypt密文，明文等值匹配永远查不到
        //and(w -> ...)给OR加上括号，生成 WHERE (username = ? OR email = ?)，避免以后再加AND条件时因优先级出错
        queryWrapper.and(w -> w.eq(User::getUsername, userLoginCommandDTO.getUsername())
                        .or()
                        .eq(User::getEmail, userLoginCommandDTO.getUsername()));

        //调用Mapper层的Api方法，根据查询条件查询用户信息
        User user = userMapper.selectOne(queryWrapper);
        //判断用户是否存在
        if (user == null) {
            //用户名/邮箱查不到用户，登录失败（不告诉用户具体是账号还是密码错，防止账号被枚举）
            throw new BusinessException("登录失败");
        }
        //校验密码是否正确
        String inputPassword = userLoginCommandDTO.getPassword().trim();
        //matches方法用于校验密码是否正确,inputPassword是用户输入的密码(明文)，user.getPassword()是数据库中的密码(密文)
        if(!PasswordEncoder.matches(inputPassword, user.getPassword()))
            throw new BusinessException("登录失败");

        //检查用户的状态
        if (!user.isActive()) {
            throw new BusinessException("用户已禁用,请联系管理员");
        }

        //生成token
        String token = JwtTokenUntil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        //构建用户信息,userinfo
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.buildUserDetailResponseDTO(user);
        UserLoginResponseDTO userLoginResponseDTO = UserConvert.buildUserLoginResponseDTO(token, userInfo);
        return userLoginResponseDTO;
    }

    /**
     * 新增用户
     * @param
     * @return
     */
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommand userRegisterCommand) {
       System.out.println(JSONUtil.parseObj(userRegisterCommand));
       //验证密码是否一致
        if (!userRegisterCommand.getPassword().equals(userRegisterCommand.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        //验证邮箱是否已存在
        LambdaQueryWrapper<User> EqueryWrapper = new LambdaQueryWrapper<>();
        EqueryWrapper.eq(User::getEmail, userRegisterCommand.getEmail());
        if (userMapper.selectCount(EqueryWrapper) > 0) {
            throw new BusinessException("邮箱已存在");
        }
        //验证用户名是否已存在
        LambdaQueryWrapper<User> UqueryWrapper = new LambdaQueryWrapper<>();
        UqueryWrapper.eq(User::getUsername, userRegisterCommand.getUsername());

        if (userMapper.selectCount(UqueryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        //用户类型
        if(!User.isValidCode(userRegisterCommand.getUserType())){
            throw new BusinessException("无效的用户类型");
        }
        //新增用户
        //密码加密
        String password = userRegisterCommand.getPassword().trim();
        //encode方法用于密码加密,encodePassword是加密后的密码
        String encodePassword = PasswordEncoder.encode(password);
        User user = UserConvert.registerCommandToEntity(userRegisterCommand, encodePassword);

        //插入数据库
        userMapper.insert(user);

        //因为要返回用户信息，所以需要将用户信息转换为DTO
        return UserConvert.buildUserDetailResponseDTO(user);
    }

    //根据用户ID查询用户信息
    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        return UserConvert.buildUserDetailResponseDTO(user);
    }
}
