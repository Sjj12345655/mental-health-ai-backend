package com.abc.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationCreateDTO {
    @Size( max = 200, message = "Session title must be less than 200 characters")
    private String sessionTitle;
    @NotBlank(message = "初始消息不能为空")
    @Size( max = 2000, message = "初始消息最多2000个字符")
    private String initialMessage;
}
