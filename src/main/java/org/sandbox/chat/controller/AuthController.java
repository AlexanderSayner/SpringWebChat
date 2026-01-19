package org.sandbox.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.User;
import org.sandbox.chat.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/vk")
    public ResponseEntity<?> handleVkAuth(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request,
            HttpServletResponse response) {

        String accessToken = payload.get("token");

        try {
            // Fetch user info from VK API
            Map<String, Object> userInfo = getVkUserInfo(accessToken);
            Map<String, Object> userData = (Map<String, Object>) ((List<Map<String, Object>>)userInfo.get("response")).get(0);

            // Create or update user
            User user = userService.findByProviderAndProviderId("vk", userData.get("id").toString())
                    .orElseGet(() -> {
                        User newUser = new User(
                                userData.get("first_name") + " " + userData.get("last_name"),
                                (String) userData.getOrDefault("email", ""),
                                "vk",
                                userData.get("id").toString()
                        );
                        return userService.save(newUser);
                    });

            // Create authentication token
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getId(),
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_USER")
            );

            // Set authentication in security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // Create session cookie
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    private Map<String, Object> getVkUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vk.com/method/users.get?fields=first_name,last_name,email&access_token=" +
                accessToken + "&v=5.131";
        return restTemplate.getForObject(url, Map.class);
    }
}
