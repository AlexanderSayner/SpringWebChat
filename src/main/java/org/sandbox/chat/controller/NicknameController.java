package org.sandbox.chat.controller;

import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.User;
import org.sandbox.chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/nickname")
@RequiredArgsConstructor
public class NicknameController {

    private final UserService userService;

    @GetMapping
    public String showNicknameForm(Authentication authentication, Model model) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        model.addAttribute("username", oauth2User.getAttribute("first_name") + " " + oauth2User.getAttribute("last_name"));
        return "nickname";
    }

    @PostMapping
    public String processNickname(
            Authentication authentication,
            @RequestParam String nickname) {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String providerId = oauth2User.getAttribute("id").toString();

        User user = userService.findByProviderAndProviderId("vk", providerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNickname(nickname);
        userService.updateNickname(user);

        return "redirect:/chat";
    }
}
