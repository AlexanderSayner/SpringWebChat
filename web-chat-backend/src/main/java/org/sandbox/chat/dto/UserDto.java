package org.sandbox.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String vkId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime createdAt;
}