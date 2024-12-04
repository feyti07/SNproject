package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.ChatMessage;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.repository.ChatMessageRepository;
import com.snim.demandesrh.service.impl.DemandeService;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {


    private final UserService userService;
    private final DemandeService demandeService;
    private final ChatMessageRepository chatMessageRepository;

    public WebSocketController(UserService userService, DemandeService demandeService, ChatMessageRepository chatMessageRepository) {
        this.userService = userService;
        this.demandeService = demandeService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @MessageMapping("/chat/{demandeId}")
    @SendTo("/topic/{demandeId}")
    public ChatMessage chat(@DestinationVariable Long demandeId, @Payload String message) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("Authentication not found in context");
        }

        // Here auth.getName() returns the email because getUsername() in UserDetails returns the email
        String email = auth.getName();
        if (email == null) {
            throw new RuntimeException("Email not found in authentication context");
        }

        System.out.println("Token is valid: " + auth);
        System.out.println("Email: " + email);

        // Check if the user exists by email
        User sender = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Long receiverId = demandeService.findUserIdByDemandeId(demandeId);
        System.out.println("Receiver ID: " + receiverId);
        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found with ID: " + receiverId));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setRoomId(demandeId.toString());

        chatMessageRepository.save(chatMessage);

        return chatMessage;
    }

}