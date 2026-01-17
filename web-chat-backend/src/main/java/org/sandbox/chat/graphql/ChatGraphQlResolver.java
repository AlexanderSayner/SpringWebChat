package org.sandbox.chat.graphql;

import org.sandbox.chat.dto.MessageDto;
import org.sandbox.chat.dto.UserDto;
import org.sandbox.chat.service.MessageService;
import org.sandbox.chat.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatGraphQlResolver {

    private final MessageService messageService;
    private final UserService userService;

    public ChatGraphQlResolver(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @QueryMapping
    public List<MessageDto> messages(@Argument Integer count) {
        if (count == null || count <= 0) {
            count = 50; // default to 50 messages
        }
        return messageService.getLatestMessages(count);
    }

    @QueryMapping
    public List<UserDto> users() {
        return userService.getAllUsers();
    }

    @MutationMapping
    public MessageDto sendMessage(@Argument MessageDto input) {
        return messageService.saveMessage(input);
    }
}