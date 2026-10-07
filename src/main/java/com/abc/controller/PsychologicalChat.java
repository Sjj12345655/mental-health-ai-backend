package com.abc.controller;

import cn.hutool.json.JSONUtil;
import com.abc.AiService.PsychologicalSupportService;
import com.abc.AiService.StructOutPut;
import com.abc.DTO.command.ConsultationCreateDTO;
import com.abc.DTO.command.ConsultationStreamDTO;
import com.abc.common.Result;
import com.abc.common.ResultCode;
import com.abc.until.JwtTokenUntil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChat {

    @Autowired
    private  PsychologicalSupportService psychologicalSupportService;
    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationCreateDTO createDTO) {
        //获取当前用户id
        String token = JwtTokenUntil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUntil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        //调用服务层方法开始会话
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, createDTO);
        return Result.success(session);
    }

    //流式接口
    @PostMapping(value = "/stream",produces = "text/event-stream")
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO consultationStreamDTO) {
        //获取当前用户id
        String token = JwtTokenUntil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUntil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();

        if(userId == null){
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(
                            Result.error(
                                    ResultCode.UNAUTHORIZED.getCode(),
                                    ResultCode.UNAUTHORIZED.getMsg(),
                                    "用户未登录")))
                    .build());
        }
        //调用服务层方法进行流式接口
        //把 AI 的逐字回复包装成`message` 事件流推给前端做打字机效果，并在全部内容发完后补发一个`done` 事件通知前端收尾
        return psychologicalSupportService.streamPsychologicalChat(consultationStreamDTO.getSessionId(), consultationStreamDTO.getUserMessage())
                .map(Fragment -> ServerSentEvent.<String>builder()
                        .event("message")
                        .data(JSONUtil.toJsonStr(Result.success(Map.of("content", Fragment,"type","normal"))))
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build())
                        .delayElements(Duration.ofMillis(50))); // 延迟50毫秒,确保流式输出的体验

        }


    }

