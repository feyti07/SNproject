package com.snim.demandesrh.entities.dto;


import com.snim.demandesrh.entities.Feedback;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.enums.SatisfactionLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private Long id;
    private Long userId; // Assuming you want to store the user ID
    private String userName; // You can keep this or omit it depending on your needs
    private SatisfactionLevel satisfactionLevel;
    private String feedback;
    private LocalDateTime createdAt;

    // Convert Feedback entity to FeedbackDto
    public static FeedbackDto fromEntity(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .userId(feedback.getUser() != null ? feedback.getUser().getId() : null) // Get the user ID
                .userName(feedback.getUserName()) // Directly using userName from Feedback
                .satisfactionLevel(feedback.getSatisfactionLevel())
                .feedback(feedback.getFeedback())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    // Convert FeedbackDto to Feedback entity
    public static Feedback toEntity(FeedbackDto dto) {
        Feedback feedback = new Feedback();
        feedback.setId(dto.getId());
        feedback.setUserName(dto.getUserName()); // Set the userName from DTO
        feedback.setSatisfactionLevel(dto.getSatisfactionLevel());
        feedback.setFeedback(dto.getFeedback());
        feedback.setCreatedAt(dto.getCreatedAt());

        // Note: Assuming you want to set the user based on userId
        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId()); // You might want to retrieve the full user object if needed
            feedback.setUser(user);
        }

        return feedback;
    }
}
