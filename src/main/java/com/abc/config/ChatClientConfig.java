package com.abc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean //chatMemory（对话记忆） 大模型本身是 失忆的 ——每次调用它都不记得上一句说了什么（每次请求独立）。这个 Bean 就是给它加"记忆"：
    public ChatMemory chatMemory() {
        return  MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build(); // 保留最新的10条消息
    }
    @Bean("open-ai") //openAiChatClient（OpenAI聊天客户端）
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory()).build()) //挂上记忆顾问，每次对话自动带上历史消息，实现多轮对话
                .defaultSystem("你是一个专业心理咨询师，温和耐心，善于倾听，能够提供专业的心理支持和建议")
                .build();

    }
}
