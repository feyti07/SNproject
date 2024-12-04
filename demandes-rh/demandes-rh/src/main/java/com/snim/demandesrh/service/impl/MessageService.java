package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.Message;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.MessageDto;
import com.snim.demandesrh.repository.DemandeRepository;
import com.snim.demandesrh.repository.MessageRepository;
import com.snim.demandesrh.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DemandeRepository demandeRepository;
    private final DemandeService demandeService;
    private final NotificationService notificationService;
    private final EmployeService employeService;
    public List<Message> getMessages(User sender, User receiver) {
        return messageRepository.findBySenderAndReceiver(sender, receiver);
    }


    public Message save(Message message) {
        System.out.println("Saving message: " + message);
        return messageRepository.save(message);
    }

    @Transactional
    public void sendMessage(Message message) {
        messageRepository.save(message); // Save the message to the database
    }

    public void sendMessage(User sender, User receiver, String content) {
        Message message = new Message();
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false); // Initially set to false or true based on your requirements
        message.setSender(sender);
        message.setReceiver(receiver);
        // Save the message entity
        messageRepository.save(message);
    }

    public Long getReceiverIdByDemandeId(Long demandeId) {
        Demande demande = demandeService.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        String matricule = demande.getEmployeeMatricule();
        Employee employee = employeService.findByMatriculeEmp(matricule)
                .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));

        return employee.getUser().getId(); // Return the receiver's user ID
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        } else {
            throw new AuthenticationCredentialsNotFoundException("Utilisateur non authentifié");
        }
    }

    public List<Message> getMessagesByDemandeId(Long demandeId) {
        return messageRepository.findByDemandeId(demandeId);
    }
    @Transactional
    public MessageDto sendMessage(String content, Long demandeId, User sender, User receiver) {
        // Create a new Message instance
        Message message = new Message();

        // Set the content of the message
        message.setContent(content);

        // Retrieve the Demande entity and set it
        message.setDemande(demandeRepository.findById(demandeId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("Demande not found")));

        // Set the sender and receiver
        message.setSender(sender); // Ensure sender is a valid User object
        message.setReceiver(receiver); // Ensure receiver is a valid User object

        // Set the sender and receiver names (if you have a way to retrieve them)
        if (sender != null) {
            message.setSenderName(sender.getName()); // Assuming User has a getName() method
        } else {
            message.setSenderName("Unknown Sender"); // Fallback in case sender is null
        }

        if (receiver != null) {
            message.setReceiverName(receiver.getName()); // Assuming User has a getName() method
        } else {
            message.setReceiverName("Unknown Receiver"); // Fallback in case receiver is null
        }

        // Save the message entity to the database
        messageRepository.save(message);

        // Convert to MessageDto and return
        return MessageDto.fromEntity(message);
    }


    public String getAuthenticatedUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities()
                    .stream()
                    .findFirst() // Get the first authority (role)
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .orElse(null); // Return null if no roles found
        }
        return null; // Return null if not authenticated
    }




   /* public Message sendMessage(User sender, User receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        return messageRepository.save(message);
    }

    public List<Message> getMessagesForUser(User user) {
        return messageRepository.findByReceiver(user);
    }*/

    public List<Message> getMessagesBetweenUsers(Long senderId, Long receiverId) {
        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }
    public List<Message> getConversationBetweenUsers(Long senderId, Long receiverId) {
        return messageRepository.findConversationBetweenUsers(senderId, receiverId);
    }

    public List<Message> findMessagesWithAdminOrRes(Long senderId, Long receiverId) {
        return messageRepository.findMessagesWhereSenderOrReceiverIsAdminOrRes(senderId, receiverId);
    }

    public List<Message> findAllMessagesForUser(Long senderId, Long receiverId) {
        return messageRepository.findMessagesBetweenUsers(senderId, receiverId);
    }


    public List<Message> findMessagesByDemandeId(Long demandeId) {
        return messageRepository.findByDemandeId2(demandeId);
    }

    public List<Message> findMessagesForUserByDemandeId(Long demandeId, Long userId) {
        return messageRepository.findByDemandeIdAndUser(demandeId, userId);
    }





    public String getSenderNameById(Long senderId) {
        return userRepository.findById(senderId.intValue())
                .map(User::getName)
                .orElseThrow(() -> new RuntimeException("Sender not found with id: " + senderId));
    }

    // Method to get receiver name by receiver ID
    public String getReceiverNameById(Long receiverId) {
        return userRepository.findById(receiverId.intValue())
                .map(User::getName)
                .orElseThrow(() -> new RuntimeException("Receiver not found with id: " + receiverId));
    }

    public Long getSenderIdByDemandeId(Long demandeId) {
        return userRepository.findById(demandeId.intValue())  // Assuming demandeId is used to find the sender
                .map(User::getId)  // Map to the user's ID instead of name
                .orElseThrow(() -> new RuntimeException("Sender not found with demande ID: " + demandeId));
    }






}