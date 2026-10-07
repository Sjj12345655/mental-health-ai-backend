package com.abc.AiService;

//返回给客户端的结构体
public class StructOutPut {

    public record StreamChatSession(
            String sessionId,
            Long userId,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            String status,
            Integer messageCount
    ) {
    }
}
