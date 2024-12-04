package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.entities.Notification;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.exceptions.NotificationNotFoundException;
import com.snim.demandesrh.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Notification createNotification(String message, String destinataire) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDestinataire(destinataire);
        notification.setDate(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String destinataire) {
        return notificationRepository.findByDestinataireAndLueFalse(destinataire);
    }


    /*private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);


    public List<Notification> getNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByRecipient(user);
        logger.info("Found {} notifications for user {}", notifications.size(), user.getId());
        return notifications;
    }

    public List<Notification> getMessagesBetweenUsers(User sender, User receiver) {
        // Récupérer toutes les notifications envoyées entre les utilisateurs
        List<Notification> sentNotifications = notificationRepository.findBySenderAndRecipient(sender, receiver);
        List<Notification> receivedNotifications = notificationRepository.findByRecipientAndSender(receiver, sender);

        System.out.println("Sent Notifications: " + sentNotifications.size());
        System.out.println("Received Notifications: " + receivedNotifications.size());

        // Ajouter également les réponses (en supposant que parentId est utilisé pour les réponses)
        sentNotifications.addAll(receivedNotifications);
        return sentNotifications;
    }


    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(User receiver, User sender, String content, LocalDateTime timestamp) {
        // Créer la notification
        Notification notification = new Notification();
        notification.setRecipient(receiver);  // Le destinataire de la notification
        notification.setSenderName(sender.getUsername());  // Nom de l'expéditeur
        notification.setMessage(content);  // Le contenu du message
        notification.setTimestamp(timestamp);  // La date du message
        notification.setRead(false);  // La notification est non lue par défaut

        // Sauvegarder la notification
        return notificationRepository.save(notification);
    }




    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public void notifyAllUsers(String message) {
        // Logique pour envoyer une notification à tous les utilisateurs
        // Cela pourrait être un système de messagerie, un WebSocket, etc.
        System.out.println(message); // Remplacez ceci par votre logique d'envoi
    }

    public void createNotification(String message, User sender, User recipient) {
        Notification notification = Notification.builder()
                .message(message)
                .sender(sender)
                .recipient(recipient)
                .isRead(false) // Notification par défaut non lue
                .timestamp(LocalDateTime.now())
                .senderName(sender.getName()) // Ou un autre attribut selon votre logique
                .build();

        notificationRepository.save(notification);
    }
*/
}
