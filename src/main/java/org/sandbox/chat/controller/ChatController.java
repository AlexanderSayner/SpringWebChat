package org.sandbox.chat.controller;

import org.sandbox.chat.model.Message;
import org.sandbox.chat.model.User;
import org.sandbox.chat.service.MessageService;
import org.sandbox.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/chat";
        } else {
            return "index";
        }
    }

    @GetMapping("/chat")
    public String chat(Authentication authentication, Model model, 
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/";
        }

        // Get user info from OAuth2 authentication
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = authentication.getAuthorities().iterator().next().getAuthority().replace("OAUTH2_", "").toLowerCase();
        
        // Find or create user
        String providerId = oauth2User.getAttribute("id").toString();
        User user = userService.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {
                User newUser = new User(
                    oauth2User.getAttribute("first_name") + " " + oauth2User.getAttribute("last_name"),
                    oauth2User.<String>getAttribute("email"),
                    provider,
                    providerId
                );
                return userService.save(newUser);
            });

        // Load recent messages
        Page<Message> messages = messageService.findLatestMessages(page, size);
        model.addAttribute("messages", messages.getContent());
        model.addAttribute("currentUser", user);

        return "chat";
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload Message message, Authentication authentication) {
        // Get user from OAuth2 authentication
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = authentication.getAuthorities().iterator().next().getAuthority().replace("OAUTH2_", "").toLowerCase();
        String providerId = oauth2User.getAttribute("id").toString();

        User user = userService.findByProviderAndProviderId(provider, providerId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        message.setUser(user);
        Message savedMessage = messageService.save(message);
        
        // Broadcast message to all connected clients
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
    }
}