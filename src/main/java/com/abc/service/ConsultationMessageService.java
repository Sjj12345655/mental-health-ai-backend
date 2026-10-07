package com.abc.service;

import com.abc.DTO.response.ConsultationMessageResponseDTO;
import com.abc.entity.ConsultationMessage;
import com.abc.mapper.ConsultationMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationMessageService {

    @Autowired
    private ConsultationMessageMapper consultationMessageMapper;

    //保存用户消息
    //把用户发的一条消息存库，记录：属于哪个会话（sessionId）、发送者类型（1=用户/2=AI）、消息类型（1=文本/2=图片…）、内容、情绪标签、发送时间
    public ConsultationMessage saveUserMessage(Long sessionId, String content ,String emotion_tag) {
        //构建用户消息实体
        ConsultationMessage userMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(1) // 1:用户, 2:AI助手,消息是谁发的
                .messageType(1) // 1:文本, 2:图片, 3:语音, 4:视频,消息类型
                .content(content) // 消息内容
                .emotionTag(emotion_tag) // 情绪标签
                .createdAt(LocalDateTime.now()) // 创建时间
                .build();

        consultationMessageMapper.insert(userMessage);
        return userMessage;
    }


    //保存AI消息到数据库
    public ConsultationMessage saveAiMessage(Long sessionId,String content,String aiModel) {
        ConsultationMessage aiMessage = ConsultationMessage.builder()
                .sessionId(sessionId)
                .senderType(2) // 1:用户, 2:AI助手,消息是谁发的
                .messageType(1) // 1:文本, 2:图片, 3:语音, 4:视频,消息类型
                .content(content) // 消息内容
                .aiModel(aiModel) // AI模型名称
                .createdAt(LocalDateTime.now()) // 创建时间
                .build();

        consultationMessageMapper.insert(aiMessage);
        return aiMessage;
    }

    //根据会话ID查询消息数量
    public Integer getMessageCountBySessionId(Long sessionId) {
        //构建查询条件
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        //查出 session_id 等于指定值的所有消息记录
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId); //==WHERE session_id = ?
        //执行查询，此时count是
        Long count = consultationMessageMapper.selectCount(queryWrapper);
        //返回消息数量
        return count.intValue();
    }

    //获取会话消息列表最后一条消息
    public ConsultationMessageResponseDTO getLatestMessageBySessionId(Long sessionId) {
        //构建查询条件
        LambdaQueryWrapper<ConsultationMessage> queryWrapper = new LambdaQueryWrapper<>();
        //查出 session_id 等于指定值的所有消息记录
        queryWrapper.eq(ConsultationMessage::getSessionId, sessionId);
        //按创建时间降序排列
        queryWrapper.orderByDesc(ConsultationMessage::getCreatedAt)
                .last("limit 1"); // 取第一条数据
        //取第一条数据
        ConsultationMessage lastMessage = consultationMessageMapper.selectOne(queryWrapper);
        if (lastMessage == null)
            return null;
        return convertToResponseDTO(lastMessage);
    }

    private ConsultationMessageResponseDTO convertToResponseDTO(ConsultationMessage message) {
        if (message == null) {
            return null;
        }

        // 手动逐字段赋值，确保转换的准确性和可控性
        ConsultationMessageResponseDTO responseDTO = new ConsultationMessageResponseDTO();
        responseDTO.setId(message.getId());
        responseDTO.setSessionId(message.getSessionId());
        responseDTO.setSenderType(message.getSenderType());
        responseDTO.setMessageType(message.getMessageType());
        responseDTO.setContent(message.getContent());
        responseDTO.setEmotionTag(message.getEmotionTag());
        responseDTO.setAiModel(message.getAiModel());
        responseDTO.setCreatedAt(message.getCreatedAt());

        // 设置描述字段（通过实体方法获取）
        responseDTO.setSenderTypeDesc(message.getSenderTypeDesc());
        responseDTO.setMessageTypeDesc(message.getMessageTypeDesc());

        // 计算消息长度
        responseDTO.calculateContentLength();

        return responseDTO;
    }

}
