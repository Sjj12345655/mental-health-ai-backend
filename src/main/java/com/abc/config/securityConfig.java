package com.abc.config;

import cn.hutool.core.text.AntPathMatcher;
import com.abc.until.JwtAuthticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class securityConfig {

    // 路径匹配器,专门用来匹配路径
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final String[] PUBLIC_PATHS={
        "/",
        "/api/test",
        "/api/user/login",
        "/api/user/add"

    };

    // 判断是否是公开路径
    public static boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (antPathMatcher.match(publicPath, path)) {
                return true;
            }
        }
        return false;
    }
    @Bean
    public JwtAuthticationFilter jwtAuthticationFilter() {
        return new JwtAuthticationFilter();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 csrf 保护(API 服务往往不需要)
                .csrf(AbstractHttpConfigurer::disable)
                //配置会话状态为无状态(JWT需要)
                .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        // SSE流式请求结束后的异步转发和错误转发，鉴权已在首次REQUEST请求时完成，直接放行
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        //公开路径，无需登录即可访问
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        //其他需求都需要认证
                        .anyRequest().authenticated()
                )
                //配置JWT过滤器,处理被拦截的
                .addFilterBefore(jwtAuthticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
