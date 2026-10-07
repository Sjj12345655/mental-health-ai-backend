package com.abc.AiService;

import com.abc.DTO.command.ConsultationCreateDTO;
import com.abc.DTO.response.ConsultationMessageResponseDTO;
import com.abc.entity.ConsultationSession;
import com.abc.exception.BusinessException;
import com.abc.service.ConsultationMessageService;
import com.abc.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationCreateDTO createDTO) {
        //创建数据库会话记录
        ConsultationSession session = consultationSessionService.createSession(userId, createDTO);
        if (session == null) {
            throw new BusinessException("用户不存在，无法创建会话");
        }
        //将初始用户消息(就是上面创建会话时传入的参数session)保存到Message这张表中
        consultationMessageService.saveUserMessage(session.getId(), createDTO.getInitialMessage(), null);

        //创建会话信息
        String sessionId = "session_" + session.getId();
        long startTime = System.currentTimeMillis();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                startTime, //会话开始时间(毫秒时间戳)
                startTime + 86400000L, //过期时间24小时
                "active",
                1
        );
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage)  { //
        //创建响应流
        return Flux.create(sink -> {
            //sink.next("数据1"); //发布数据
            //sink.complete(); //完成发布
            //sink.error(new RuntimeException("错误")); //发布错误
            Long dbSessionId = extractSessionId(sessionId);
            if (dbSessionId == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            } else {
              //检查是否为初始消息 ,避免重复保存
                boolean isInitialMessage = false; // 是否为初始消息
                Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
                if (messageCount == 1) {
                    ConsultationMessageResponseDTO latestMessage = consultationMessageService.getLatestMessageBySessionId(dbSessionId);
                    if (latestMessage != null && latestMessage.getSenderType().equals(1) && latestMessage.getContent().equals(userMessage)) {
                        isInitialMessage = true;
                    }
                }
                // 如果不是初始消息，则保存用户消息
                if (!isInitialMessage) {
                    consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
                }
                // 每个会话独立的记忆空间，历史消息的读取和写入由 MessageChatMemoryAdvisor 自动完成，无需手动 add
                String conversationId = "conversation_" + sessionId;

                //用于存储AI完成的响应
                StringBuilder aiResponse = new StringBuilder();

                //使用chatClient发送消息到OpenAI
               chatClient.prompt()
                       .system(ProManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
                       .user(userMessage)
                       .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId))
                       .stream()
                       .content()
                       .doOnNext(fragment ->{
                           aiResponse.append(fragment); // 将Fragment追加到aiResponse中,拼接到一起
                           sink.next(fragment);  //Fragment返回每个片段的内容
                       })

                       .doOnComplete(() ->{
                           //StringBuilder 重写了toString()，让它返回"容器里所有碎片拼起来的最终字符串"
                           String  completeResponse = aiResponse.toString();
                           //将AI返回的内容保存到数据库中（记忆由 advisor 自动更新，这里只需持久化）
                           consultationMessageService.saveAiMessage(dbSessionId, completeResponse, "open-ai");

                           sink.complete(); // 完成发布
                       })
                       .doOnError(error ->{
                           sink.error(error); // 发布错误
                       })
                       .subscribe(); //订阅并启动流
            }
        });

    }

    //获取参数中的sessionid
    public Long extractSessionId(String sessionId) {
       if(sessionId != null && sessionId.startsWith("session_")) {
           // 提取sessionid中的数字部分
           String idstr = sessionId.substring("session_".length());
           return Long.parseLong(idstr);
       }
       return null;
    }
}
