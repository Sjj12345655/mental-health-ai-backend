package com.abc.until;

import cn.hutool.json.JSONUtil;
import com.abc.DTO.response.UserLoginResponseDTO;
import com.abc.common.ResultCode;
import com.abc.config.securityConfig;
import com.abc.enumClass.UserStatus;
import com.abc.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
// JWT认证过滤器,OncePerRequestFilter确保每个请求只被过滤一次(固定写法)
public class JwtAuthticationFilter extends OncePerRequestFilter {
    @Resource
    private UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        //检查是非为公开路径
        return securityConfig.isPublicPath(requestURI);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //获取请求的url和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();


        //1.提取token
        String token = JwtTokenUntil.extractToken(request);

        //StringUtils.hasText(token)判断字符串是否包含有效文本
        if(StringUtils.hasText(token)){
            //2.验证token,并获取用户信息,validateResult是解密后的结果
            JwtTokenUntil.TokenResult validateResult;
            try {
                validateResult = JwtTokenUntil.validateToken(token);
            } catch (Exception e) {
                // token过期、签名错误、格式错误等验签异常，统一按未登录处理(401)，不能让异常抛成500
                log.warn("token校验失败: {}", e.getMessage());
                clearContext();
                ResponseUntil.writeError(response, ResultCode.TOKEN_EXPIRED);
                return;
            }
            if(validateResult != null && validateResult.isValid()){
                //3.查询用户信息验证用户的状态
                UserLoginResponseDTO.UserDetailResponseDTO  user = userService.getUserById(validateResult.getUserId());
                if(user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())){
                    //JWT 认证通过后，要告诉 Spring Security"这个请求是谁、有什么权限"
                    //以下就是安全框架认证对象的创建和上下文的设置
                   //4.创建Spring Security的认证对象

                    // 4.1封装权限列表（SimpleGrantedAuthority 是 GrantedAuthority 的标准实现）
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_USER" + validateResult.getRoleType())
                    );
                    //4.2创建UserPasswordAuthenticationToken对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken (
                            validateResult.getUserId(), //用户名作为主题
                            null , //密码为null
                            authorities); //权限列表，就是上面的 authorities
                    // 4.3 设置认证信息到Spring Security上下文 ,不设置认证信息，Spring Security会认为当前请求是未认证的
                    //会进行拦截，返回401
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    //将token存储到请求属性中，方便后续获取
                    request.setAttribute("jwtToken", token);
                }else {
                    //用户状态异常，清除上下文并返回错误
                    clearContext();
                    ResponseUntil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                }

            }else {
                //有异常就要清除上下文
                clearContext();
                ResponseUntil.writeError(response,ResultCode.TOKEN_INVALID);
            }

        }else {
            //清除上下文
            clearContext();
            ResponseUntil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        //继续执行下一个过滤器
        filterChain.doFilter(request, response);
    }

    //清除Spring Security上下文
    private void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
