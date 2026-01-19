package org.sandbox.chat.service;

import lombok.RequiredArgsConstructor;
import org.sandbox.chat.model.Message;
import org.sandbox.chat.model.User;
import org.sandbox.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message save(Message message) {
        return messageRepository.save(message);
    }

    public Page<Message> findLatestMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByOrderByTimestampDesc(pageable);
    }
}