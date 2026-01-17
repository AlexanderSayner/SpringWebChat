package org.sandbox.chat.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/user")
    public Map<String, Object> getUser(Authentication authentication) {
        Map<String, Object> userData = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            userData.put("authenticated", true);
            userData.put("name", authentication.getName());
            
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                userData.put("attributes", oauth2User.getAttributes());
                
                // Extract specific attributes if available
                if (oauth2User.getAttribute("name") != null) {
                    userData.put("displayName", oauth2User.getAttribute("name"));
                } else if (oauth2User.getAttribute("first_name") != null && oauth2User.getAttribute("last_name") != null) {
                    userData.put("displayName", 
                        oauth2User.getAttribute("first_name") + " " + 
                        oauth2User.getAttribute("last_name"));
                }
                
                if (oauth2User.getAttribute("email") != null) {
                    userData.put("email", oauth2User.getAttribute("email"));
                }
            }
        } else {
            userData.put("authenticated", false);
        }
        
        return userData;
    }
}