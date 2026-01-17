package org.sandbox.chat.service;

import org.sandbox.chat.dto.UserDto;
import org.sandbox.chat.entity.User;
import org.sandbox.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    
    public Optional<UserDto> findByVkId(String vkId) {
        return userRepository.findByVkId(vkId)
                .map(user -> modelMapper.map(user, UserDto.class));
    }
    
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> modelMapper.map(user, UserDto.class));
    }
    
    @Transactional
    public UserDto save(User user) {
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }
    
    public java.util.List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}