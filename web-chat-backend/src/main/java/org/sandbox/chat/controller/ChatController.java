package org.sandbox.chat.controller;

import lombok.RequiredArgsConstructor;
import org.sandbox.chat.dto.MessageDto;
import org.sandbox.chat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    @GetMapping("/api/chat/latest-messages")
    public ResponseEntity<?> getLatestMessages(@RequestParam(defaultValue = "50") int count) {
        List<MessageDto> messages = messageService.getLatestMessages(Math.min(count, 100)); // max 100 messages
        return ResponseEntity.ok(messages);
    }
}