package com.snim.demandesrh.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Indexed;

import java.time.LocalDateTime;

@Data
@Entity
public class EmailConfirmationToken {
    @Id
    private String id;
    private String token;
    @CreatedDate
    @ReadOnlyProperty
    private LocalDateTime timeStamp;

}
