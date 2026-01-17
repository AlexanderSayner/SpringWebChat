package org.sandbox.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private UserDto user;
    private String content;
    private LocalDateTime createdAt;
}