package org.sandbox.chat.controller;

import org.sandbox.chat.dto.MessageDto;
import org.sandbox.chat.dto.UserDto;
import org.sandbox.chat.dto.WebSocketMessage;
import org.sandbox.chat.entity.Message;
import org.sandbox.chat.entity.User;
import org.sandbox.chat.service.MessageService;
import org.sandbox.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload WebSocketMessage webSocketMessage) {
        // For now, we'll create a simple message without authentication
        // In a real scenario, we would get the authenticated user from the session
        User user = new User();
        user.setId(1L);
        user.setUsername("Anonymous");
        
        MessageDto savedMessage = messageService.saveMessage(user, webSocketMessage.getContent());
        
        WebSocketMessage response = new WebSocketMessage();
        response.setType("message");
        response.setMessage(savedMessage);
        
        messagingTemplate.convertAndSend("/topic/messages", response);
    }
}