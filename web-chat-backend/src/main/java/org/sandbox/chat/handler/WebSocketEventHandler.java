package org.sandbox.chat.handler;

import org.sandbox.chat.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class WebSocketEventHandler {

    private static final CopyOnWriteArraySet<String> connectedUsers = new CopyOnWriteArraySet<>();

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new web socket connection");
        
        // Get the STOMP headers to extract user information
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username != null && connectedUsers.add(username)) {
            log.info("User Connected: {}", username);
            
            // Broadcast to all users that someone joined
            WebSocketMessage message = new WebSocketMessage();
            message.setType("userJoined");
            message.setContent(username + " joined the chat");
            messagingTemplate.convertAndSend("/topic/messages", message);
            
            // Send updated user list
            sendOnlineUsersList();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            connectedUsers.remove(username);
            log.info("User Disconnected: {}", username);

            WebSocketMessage message = new WebSocketMessage();
            message.setType("userLeft");
            message.setContent(username + " left the chat");
            messagingTemplate.convertAndSend("/topic/messages", message);
            
            // Send updated user list
            sendOnlineUsersList();
        }
    }
    
    private void sendOnlineUsersList() {
        WebSocketMessage userListMessage = new WebSocketMessage();
        userListMessage.setType("userList");
        userListMessage.setContent(String.join(",", connectedUsers));
        messagingTemplate.convertAndSend("/topic/users", userListMessage);
    }
}