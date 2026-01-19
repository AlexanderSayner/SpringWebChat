package org.sandbox.chat.security;

import org.sandbox.chat.model.User;
import org.sandbox.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

            // Handle VK OAuth2 differently since VK API requires special handling
            if ("vk".equals(registrationId)) {
                return processVkUser(attributes);
            }

            return oAuth2User;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processVkUser(Map<String, Object> attributes) {
        // VK API returns user data in a different format
        // Need to make another call to get full user information
        
        // Extract access token and user ID from the OAuth2 response
        String accessToken = extractAccessToken(attributes);
        Long userId = extractUserId(attributes);
        
        if (userId == null || accessToken == null) {
            throw new RuntimeException("Could not extract user ID or access token from VK OAuth response");
        }
        
        // Create a new map with normalized attributes
        Map<String, Object> normalizedAttributes = new HashMap<>();
        normalizedAttributes.put("id", userId);
        normalizedAttributes.put("provider", "vk");

        // Get extended user info from VK API
        if (accessToken != null) {
            RestTemplate restTemplate = new RestTemplate();
            String userInfoUrl = String.format(
                "https://api.vk.com/method/users.get?user_ids=%d&fields=first_name,last_name,photo_100,email&access_token=%s&v=5.131",
                userId, accessToken
            );
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(userInfoUrl, Map.class);
                if (response != null && response.containsKey("response")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> users = (java.util.List<Map<String, Object>>) response.get("response");
                    if (!users.isEmpty()) {
                        Map<String, Object> userResponse = users.get(0);
                        
                        normalizedAttributes.put("first_name", userResponse.get("first_name"));
                        normalizedAttributes.put("last_name", userResponse.get("last_name"));
                        normalizedAttributes.put("email", userResponse.get("email")); // May be null if user didn't share email
                        normalizedAttributes.put("photo_url", userResponse.get("photo_100"));
                    }
                }
            } catch (Exception e) {
                // Log error but continue with basic info
                System.err.println("Error fetching extended VK user info: " + e.getMessage());
            }
        }

        // Find or create user in our database
        User user = userService.findByProviderAndProviderId("vk", userId.toString())
            .orElseGet(() -> {
                String firstName = (String) normalizedAttributes.get("first_name");
                String lastName = (String) normalizedAttributes.get("last_name");
                String fullName = (firstName != null ? firstName : "") + 
                                 (lastName != null ? " " + lastName : "");
                
                User newUser = new User(
                    fullName.trim(),
                    (String) normalizedAttributes.get("email"),
                    "vk",
                    userId.toString()
                );
                return userService.createOrGet(newUser);
            });

        // Update normalized attributes with user data
        normalizedAttributes.put("username", user.getUsername());
        normalizedAttributes.put("email", user.getEmail());

        return new CustomOAuth2User(normalizedAttributes);
    }
    
    private String extractAccessToken(Map<String, Object> attributes) {
        // Try different possible keys for access token
        if (attributes.containsKey("access_token")) {
            return (String) attributes.get("access_token");
        }
        
        // Check if we have a token object
        Object tokenObj = attributes.get("tokenResponse");
        if (tokenObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenMap = (Map<String, Object>) tokenObj;
            return (String) tokenMap.get("access_token");
        }
        
        return null;
    }
    
    private Long extractUserId(Map<String, Object> attributes) {
        // Try different possible keys for user ID
        if (attributes.containsKey("user_id")) {
            Object userIdObj = attributes.get("user_id");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            }
        }
        
        // Also check for 'id' field in case it's available
        if (attributes.containsKey("id")) {
            Object idObj = attributes.get("id");
            if (idObj instanceof Integer) {
                return ((Integer) idObj).longValue();
            } else if (idObj instanceof Long) {
                return (Long) idObj;
            } else if (idObj instanceof String) {
                return Long.parseLong((String) idObj);
            }
        }
        
        return null;
    }
}