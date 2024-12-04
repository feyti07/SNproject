package com.snim.demandesrh.entities.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.snim.demandesrh.entities.Message;
import com.snim.demandesrh.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private Long sender;
    private Long receiver;
    private Long demandeId;
    private String senderName;
    private String receiverName;
    private String attachmentPath;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private String contentType; // Ajout du champ contentType

    @JsonCreator
    public MessageDto(@JsonProperty("content") String content,
                      @JsonProperty("demandeId") Long demandeId,
                      @JsonProperty("sender") Long sender,
                      @JsonProperty("receiver") Long receiver,
                      @JsonProperty("timestamp") LocalDateTime timestamp,
                      @JsonProperty("isRead") boolean isRead,
                      @JsonProperty("senderName") String senderName,
                      @JsonProperty("receiverName") String receiverName,
                      @JsonProperty("attachmentPath") String attachmentPath,
                      @JsonProperty("fileData") byte[] fileData,
                      @JsonProperty("contentType") String contentType) { // Ajout de contentType au constructeur
        this.content = content;
        this.demandeId = demandeId;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.isRead = isRead;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.attachmentPath = attachmentPath != null ? attachmentPath : "";
        this.fileData = fileData;
        this.contentType = contentType; // Assignation de contentType
    }

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .timestamp(message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now())
                .isRead(message.isRead())
                .sender(message.getSender() != null ? message.getSender().getId() : null)
                .receiver(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .demandeId(message.getDemande() != null ? message.getDemande().getId() : null)
                .senderName(message.getSenderName() != null ? message.getSenderName() : "Unknown Sender")
                .receiverName(message.getReceiverName() != null ? message.getReceiverName() : "Unknown Receiver")
                .attachmentPath(message.getAttachmentPath() != null ? message.getAttachmentPath() : "")
                .fileData(message.getFileData())
                .contentType(message.getContentType()) // Ajout de contentType ici
                .build();
    }

    public static Message toEntity(MessageDto dto, User sender, User receiver) {
        Message message = new Message();
        message.setId(dto.getId());
        message.setContent(dto.getContent());
        message.setTimestamp(dto.getTimestamp());
        message.setRead(dto.isRead());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSenderName(dto.getSenderName());
        message.setReceiverName(dto.getReceiverName());
        message.setAttachmentPath(dto.getAttachmentPath());
        message.setFileData(dto.getFileData());
        message.setContentType(dto.getContentType()); // Assignation de contentType à l'entité
        return message;
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "id=" + (id != null ? id : "null") +
                ", content='" + (content != null ? content : "null") + '\'' +
                ", timestamp=" + (timestamp != null ? timestamp : "null") +
                ", isRead=" + isRead +
                ", sender=" + (sender != null ? sender : "null") +
                ", receiver=" + (receiver != null ? receiver : "null") +
                ", demandeId=" + (demandeId != null ? demandeId : "null") +
                ", senderName='" + (senderName != null ? senderName : "null") + '\'' +
                ", receiverName='" + (receiverName != null ? receiverName : "null") + '\'' +
                ", attachmentPath='" + (attachmentPath != null ? attachmentPath : "null") + '\'' +
                ", fileData=" + (fileData != null ? "present" : "null") +
                ", contentType='" + (contentType != null ? contentType : "null") + '\'' + // Affiche si contentType est présent
                '}';
    }
}