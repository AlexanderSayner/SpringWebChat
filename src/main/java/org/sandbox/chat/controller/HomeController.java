package org.sandbox.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final Environment environment;

    //@GetMapping("/")
    public String home(Authentication authentication, Model model) {
        String vkAppId = environment.getRequiredProperty("VK_APP_ID");
        String vkRedirectUrl = environment.getRequiredProperty("VK_REDIRECT_URL");

        model.addAttribute("vkAppId", vkAppId);
        model.addAttribute("vkRedirectUrl", vkRedirectUrl);

        return authentication != null && authentication.isAuthenticated()
                ? "redirect:/chat"
                : "index";
    }
}
