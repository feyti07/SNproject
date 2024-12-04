package com.snim.demandesrh.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private Long receiverId;  // ID of the user who will receive the message
    private Long demandeId;   // ID of the demande associated with the message
    private String content;
}
