package org.sandbox.chat.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User delegate;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Map<String, Object> attributes) {
        this.delegate = new DefaultOAuth2User(null, attributes, "id");
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return attributes.get("id") != null ? attributes.get("id").toString() : 
               attributes.get("username") != null ? attributes.get("username").toString() : 
               "unknown";
    }
}