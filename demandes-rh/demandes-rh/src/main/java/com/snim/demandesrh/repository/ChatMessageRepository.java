package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.ChatMessage;
import com.snim.demandesrh.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomId(String roomId);
}
