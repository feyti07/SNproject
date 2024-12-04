package com.snim.demandesrh.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequest {

    private Long receiverId;
    private Long senderId;
    private String content;
}
