package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.Notification;
import com.snim.demandesrh.entities.NotificationRequest;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.repository.NotificationRepository;
import com.snim.demandesrh.service.impl.NotificationService;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{username}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String username) {
        List<Notification> notifications = notificationService.getNotifications(username);
        return ResponseEntity.ok(notifications);
    }
   /* private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsForUser(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest notificationRequest) {
        // Rechercher le destinataire (receiver) par son ID
        User receiver = userService.findById(notificationRequest.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found with ID: " + notificationRequest.getReceiverId()));

        // Rechercher l'expéditeur (sender) par son ID
        User sender = userService.findById(notificationRequest.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found with ID: " + notificationRequest.getSenderId()));

        // Créer la notification
        Notification notification = notificationService.createNotification(receiver, sender, notificationRequest.getContent(), LocalDateTime.now());

        // Retourner la notification créée
        return new ResponseEntity<>(notification, HttpStatus.CREATED);
    }

    @GetMapping("/generateMessage")
    public ResponseEntity<String> generateNotificationMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {

        // Récupérer l'expéditeur (sender) et le destinataire (receiver)
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found with ID: " + senderId));

        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found with ID: " + receiverId));

        // Créer le message
        String formattedMessage = sender.getUsername() + " vous a envoyé un suivi le " + LocalDateTime.now() + ". Message: " + content;

        // Sauvegarder la notification si nécessaire
        notificationService.createNotification(receiver, sender, content, LocalDateTime.now());

        // Retourner le message formaté dans la réponse
        return ResponseEntity.ok(formattedMessage);
    }


*/

}
