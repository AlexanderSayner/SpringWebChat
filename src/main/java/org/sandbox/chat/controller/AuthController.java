package org.sandbox.chat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.User;
import org.sandbox.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/vk")
    public ResponseEntity<String> handleVkAuth(@RequestBody Map<String, String> payload, HttpSession session) {
        String accessToken = payload.get("token");

        // Use the token to fetch user information from VK API
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vk.com/method/users.get?fields=first_name,last_name,email&access_token=" + accessToken + "&v=5.131";
        Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

        // Extract user information
        List<Map<String, Object>> responseList = (List<Map<String, Object>>) userInfo.get("response");
        Map<String, Object> userData = responseList.get(0);

        // Create or update user in your system
        User user = new User(
                userData.get("first_name") + " " + userData.get("last_name"),
                String.valueOf(userData.get("email")),
                "vk",
                String.valueOf(userData.get("id"))
        );
        userService.save(user);

        // Set user in session or security context
        session.setAttribute("user", user);

        return ResponseEntity.ok("Authentication successful");
    }
}

