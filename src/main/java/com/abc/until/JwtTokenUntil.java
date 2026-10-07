package com.abc.until;

import com.abc.config.JwtConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.observation.transport.ResponseContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

// 实现ApplicationContextAware接口，获取applicationContext
@Component
public class JwtTokenUntil implements ApplicationContextAware {
    // 签发人
    private static final String ISSUER = "abc";

    private static ApplicationContext applicationContext;
    // 用于在静态工具类中获取Spring容器管理的bean
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUntil.applicationContext = applicationContext;
    }

    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    // 生成token的方法
    public static String generateToken(Long userId, String username, Integer roleType) {
        try {
            // 获取jwt的配置信息
            JwtConfig jwtConfig = getJwtConfig();
            //生成算法的签名
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            //生成过期的时间
            Date gxtime = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(gxtime)  //过期时间
                    .withIssuedAt(new Date()) // 添加 签发 时间
                    .withIssuer(ISSUER) // 签发人
                    .sign(algorithm); // 签名
            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败" + e);
        }

    }
    //提取token
    public static String extractToken(HttpServletRequest request) {
        if (request == null) return null;
        JwtConfig jwtConfig = getJwtConfig();
        // 请求头名称从配置读取（yml中配置的是 Authorization）
        String header = request.getHeader(jwtConfig.getHeader());
        String prefix = jwtConfig.getTokenPrefix();
        // 必须同时满足：有文本 + 以前缀开头
        if(StringUtils.hasText(header) && header.startsWith(prefix)){
            return header.substring(prefix.length()).trim();
        }
        // 不满足前缀，直接返回null，不进入JWT校验
        return null;
    }

    //token结果验证封装类
    @Getter
    public static class TokenResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        // 是否有效
        private final boolean valid;


        public TokenResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }


    }

    //验证token,从真 token 里把用户信息取出来并检查完整性
    public static TokenResult validateToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        Long userId = decodedJWT.getClaim("userId").asLong();
        String username = decodedJWT.getClaim("username").asString();
        // 获取角色类型,不确定是否是int类型，所以尝试转换
        Integer roleType = null;
        try {
            roleType = decodedJWT.getClaim("roleType").asInt();
        } catch (Exception e) {
            // 如果转换失败，roleType转换成String类型
            String roleTypeStr = decodedJWT.getClaim("roleType").asString();
            if(StringUtils.hasText(roleTypeStr)){
                roleType = Integer.valueOf(roleTypeStr);
            }
        }
        //判断一下取出的数据是否为空
        if (userId != null && StringUtils.hasText(username) && roleType != null) {
            return new TokenResult(userId, username, roleType, true);
        }
        return null;
    }

    //验证token有效性,证明 token 是真的
    public static DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token))
            throw new RuntimeException("token不能为空");

        //token解码
        JwtConfig jwtConfig = getJwtConfig();
        //生成加密算法
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        //解码token
        return JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }

    //获取当前token
    //在普通静态方法/工具类里，拿到"当前这个 HTTP 请求"的 request 对象 。
    public static String getCurrentToken() {
        // ServletRequestAttributes是 Spring 提供的"在任意代码位置获取当前 HTTP 请求"的标准手段，底层靠 ThreadLocal 实现。
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwtToken");
            if (token != null)
                return token;
            //备用方法，从请求头中获取token
            String headerToken = extractToken(request);
            if (headerToken != null)
                return headerToken;
        }
        return null;
    }
}
