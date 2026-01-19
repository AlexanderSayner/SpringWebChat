package org.sandbox.chat.service;

import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.User;
import org.sandbox.chat.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public User createOrGet(User user) {
        return userRepository.findByProviderAndProviderId(user.getProvider(), user.getProviderId())
                .orElseGet(() -> userRepository.save(user));
    }

}