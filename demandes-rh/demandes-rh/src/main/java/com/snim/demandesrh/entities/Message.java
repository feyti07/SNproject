package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.Base64;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String senderName;

    private String receiverName;

    private String attachmentPath;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private String contentType; // Ajout du champ contentType

    // Méthodes existantes...

    public String getAttachmentFileName() {
        if (attachmentPath != null) {
            return attachmentPath.substring(attachmentPath.lastIndexOf("/") + 1);
        }
        return null;
    }

    // Ajout de setter et getter pour contentType
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}