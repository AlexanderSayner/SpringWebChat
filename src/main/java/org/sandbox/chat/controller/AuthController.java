package org.sandbox.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/vk")
    public ResponseEntity<String> handleVkAuth(@RequestBody Map<String, String> payload) {
        String accessToken = payload.get("token");

        // Fetch user info from VK API
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vk.com/method/users.get?fields=first_name,last_name,email&access_token=" + accessToken + "&v=5.131";
        String userInfoResponse = restTemplate.getForObject(url, String.class);

        try {
            // Parse the response from VK API
            Map<String, Object> userInfoMap = objectMapper.readValue(userInfoResponse, Map.class);
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) userInfoMap.get("response");
            if (responseList == null || responseList.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid user info response from VK");
            }
            Map<String, Object> userData = responseList.get(0);

            String vkId = userData.get("id").toString();
            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String email = (String) userData.get("email");

            // Create authorities (roles)
            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");

            // Create an OAuth2User object
            OAuth2User oauth2User = new DefaultOAuth2User(
                    authorities,
                    Map.of(
                            "id", vkId,
                            "first_name", firstName,
                            "last_name", lastName,
                            "email", email
                    ),
                    "id"
            );

            // Create an authentication token
            OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                    oauth2User,
                    authorities,
                    "vk"
            );

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return ResponseEntity.ok("Аутентификация успешна");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing user info");
        }
    }
}
