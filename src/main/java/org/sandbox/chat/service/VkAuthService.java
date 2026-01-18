package org.sandbox.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VkAuthService {

    public String getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.vk.com/method/users.get?fields=first_name,last_name,email&access_token=" + accessToken + "&v=5.131";
        return restTemplate.getForObject(url, String.class);
    }
}

