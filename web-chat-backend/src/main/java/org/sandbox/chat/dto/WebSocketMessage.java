package org.sandbox.chat.dto;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type; // 'message', 'userJoined', 'userLeft'
    private MessageDto message;
    private UserDto user;
    private String content;
}