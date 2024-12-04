package com.snim.demandesrh.controller;
import com.snim.demandesrh.entities.Feedback;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.FeedbackDto;
import com.snim.demandesrh.service.impl.FeedbackService;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private UserService userService;
    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<?> addFeedback(@RequestBody Feedback feedback) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    String email = userDetails.getUsername();

                    Optional<User> optionalUser = userService.findByEmail(email);
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        feedback.setUser(user);
                        feedback.setUserName(user.getName());

                        Feedback savedFeedback = feedbackService.addFeedback(feedback);

                        // Returning only a success message or minimal feedback information
                        Map<String, String> response = new HashMap<>();
                        response.put("message", "Feedback submitted successfully");
                        response.put("feedbackId", String.valueOf(savedFeedback.getId()));

                        return ResponseEntity.ok(response);  // Returning a simple response
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        } catch (Exception e) {
            e.printStackTrace();  // Log the full exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while saving feedback"));
        }
    }

    @GetMapping
    public ResponseEntity<List<FeedbackDto>> getAllFeedback() {
        List<FeedbackDto> feedbackDtos = feedbackService.getAllFeedback().stream()
                .map(FeedbackDto::fromEntity) // Convert each Feedback entity to FeedbackDto
                .collect(Collectors.toList());

        // Log the Feedback DTOs for debugging
        System.out.println("Feedback DTOs: " + feedbackDtos);

        return ResponseEntity.ok(feedbackDtos); // Return the list wrapped in a ResponseEntity
    }

    @GetMapping("/level")
    public Map<String, Long> getSatisfactionLevels() {
        return feedbackService.getSatisfactionLevels(); // Return satisfaction levels
    }
}
