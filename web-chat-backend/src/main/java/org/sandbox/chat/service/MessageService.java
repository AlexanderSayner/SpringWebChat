package org.sandbox.chat.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.sandbox.chat.dto.MessageDto;
import org.sandbox.chat.dto.UserDto;
import org.sandbox.chat.entity.Message;
import org.sandbox.chat.entity.User;
import org.sandbox.chat.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ModelMapper modelMapper;

    public List<MessageDto> getLatestMessages(int count) {
        Pageable pageable = PageRequest.of(0, count);
        return messageRepository.findLatestMessages(pageable).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto saveMessage(User user, String content) {
        Message message = new Message();
        message.setUser(user);
        message.setContent(content);

        Message savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Transactional
    public MessageDto saveMessage(MessageDto messageDto) {
        // For now, we'll create a basic message - in a real scenario you'd need to properly map the DTO
        // and handle user association properly
        Message message = new Message();
        message.setContent(messageDto.getContent());
        
        // If the DTO contains user info, we might want to set it, but for now we'll keep it simple
        // This would require getting the user from the authentication context
        
        Message savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }

    private MessageDto convertToDto(Message message) {
        MessageDto dto = modelMapper.map(message, MessageDto.class);
        if (message.getUser() != null) {
            dto.setUser(modelMapper.map(message.getUser(), UserDto.class));
        }
        return dto;
    }
}