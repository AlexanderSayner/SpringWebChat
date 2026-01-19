package org.sandbox.chat.controller;

import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.Message;
import org.sandbox.chat.model.User;
import org.sandbox.chat.service.MessageService;
import org.sandbox.chat.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                ? "redirect:/chat"
                : "index";
    }

    @GetMapping("/chat")
    public String chat(Authentication authentication, Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/";
        }

        // Extract user info from authentication
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = extractProvider(authentication);
        String providerId = oauth2User.getAttribute("id").toString();

        // Find or create user
        User user = userService.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> createNewUser(oauth2User, provider, providerId));

        // Load messages and add to model
        prepareChatModel(model, page, size, user);
        return "chat";
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload Message message, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        message.setUser(user);

        Message savedMessage = messageService.save(message);
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);
    }

    private String extractProvider(Authentication authentication) {
        return authentication.getAuthorities().iterator().next()
                .getAuthority()
                .replace("OAUTH2_", "")
                .toLowerCase();
    }

    private User createNewUser(OAuth2User oauth2User, String provider, String providerId) {
        return userService.save(new User(
                getFullName(oauth2User),
                oauth2User.getAttribute("email"),
                provider,
                providerId
        ));
    }

    private String getFullName(OAuth2User oauth2User) {
        return oauth2User.getAttribute("first_name") + " " + oauth2User.getAttribute("last_name");
    }

    private User getAuthenticatedUser(Authentication authentication) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = extractProvider(authentication);
        String providerId = oauth2User.getAttribute("id").toString();

        return userService.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void prepareChatModel(Model model, int page, int size, User user) {
        Page<Message> messages = messageService.findLatestMessages(page, size);
        model.addAttribute("messages", messages.getContent());
        model.addAttribute("currentUser", user);
    }
}
