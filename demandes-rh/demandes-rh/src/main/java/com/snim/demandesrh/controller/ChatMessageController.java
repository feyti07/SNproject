package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.ChatMessage;
import com.snim.demandesrh.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatMessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessage>> getMessagesForRoom(@PathVariable String roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }
}
