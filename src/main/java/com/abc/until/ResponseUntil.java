package com.abc.until;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.abc.common.Result;
import com.abc.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseUntil {
    //过滤器的异常相应
    public static void writeError(HttpServletResponse response, ResultCode code){
        //根据不同响应码返回不同的结果
        int status = switch (code) {
            case UNAUTHORIZED,ACCESS_UNAUTHORIZED,TOKEN_INVALID,TOKEN_EXPIRED,
                    TOKEN_BLOCKED -> HttpStatus.HTTP_UNAUTHORIZED; // 401 Unauthorized
            case TOKEN_ACCESS_FORBIDDEN -> HttpStatus.HTTP_FORBIDDEN; // token禁止访问403 Forbidden
            default -> HttpStatus.HTTP_BAD_REQUEST; // 400 Bad Request

        };
        //设置响应状态码和内容类型为json,字符编码为utf-8
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 设置字符编码为utf-8

        try (PrintWriter writer = response.getWriter()){
            String jsonResponse = JSONUtil.toJsonStr(Result.error(code.getCode(), code.getMsg(), null));
            writer.print(jsonResponse);
            writer.flush(); // 确保响应被写入
        }catch (IOException e){
            log.info("响应异常", e.getMessage());

        }

    }


}
