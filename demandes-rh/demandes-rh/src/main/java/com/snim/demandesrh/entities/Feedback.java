package com.snim.demandesrh.entities;

import com.snim.demandesrh.enums.SatisfactionLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String userName;
    private SatisfactionLevel satisfactionLevel;
    private String feedback;
    private LocalDateTime createdAt;


}

