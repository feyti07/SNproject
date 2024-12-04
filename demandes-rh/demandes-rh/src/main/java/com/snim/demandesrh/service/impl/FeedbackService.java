package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.entities.Feedback;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.repository.FeedbackRepository;
import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback addFeedback(Feedback feedback) {
        feedback.setCreatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Map<String, Long> getSatisfactionLevels() {
        List<Feedback> feedbackList = feedbackRepository.findAll();
        Map<String, Long> satisfactionCount = new HashMap<>();

        // Count the occurrences of each satisfaction level
        for (Feedback feedback : feedbackList) {
            String level = feedback.getSatisfactionLevel().name(); // Use name() to get the string representation
            satisfactionCount.put(level, satisfactionCount.getOrDefault(level, 0L) + 1);
        }

        return satisfactionCount; // Return the counts
    }
}
