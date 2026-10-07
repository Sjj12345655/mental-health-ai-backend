package com.abc.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.abc.DTO.command.ConsultationCreateDTO;
import com.abc.entity.ConsultationSession;
import com.abc.mapper.ConsultationSessionMapper;
import com.abc.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;
    //用户发起一次新对话时，创建一条"会话"记录（属于谁、标题、开始时间）。没传标题就自动生成`"帅哥AI助手 + 当前时间"` ，然后入库
    public ConsultationSession createSession(Long userId, ConsultationCreateDTO createDTO) {
        //验证用户是否存在
        if (userMapper.selectById(userId) != null) {
            //创建会话记录
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(createDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            //如果为提供标题，则使用默认标题
            if (StrUtil.isBlank(createDTO.getSessionTitle())) {
                session.setSessionTitle("帅哥AI助手" + DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
            }

            //插入记录
            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }
}
