package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Notification {
  /*  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;


    private String message;

    private boolean isRead;

    private LocalDateTime timestamp;
    private String senderName;
    private Long parentId;*/
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

    private String message;
    private LocalDateTime date;
    private String destinataire;
  @Column(name = "lue", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
  private Boolean lue = false;
}
