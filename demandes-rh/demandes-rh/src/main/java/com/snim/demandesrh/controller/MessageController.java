package com.snim.demandesrh.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snim.demandesrh.entities.*;
import com.snim.demandesrh.entities.dto.DemandeDto;
import com.snim.demandesrh.entities.dto.MessageDto;
import com.snim.demandesrh.entities.dto.MessageRequest;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.repository.DemandeRepository;
import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.impl.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/messages")

public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private DemandeService demandeService;
    @Autowired
    private EmployeService employeService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private NotificationService notificationService;


    private DemandeDto demandeDto;
    private Employee employee;

   /* @GetMapping("/messages")
    public ResponseEntity<List<String>> getMessagesBetweenUsers(
            @RequestParam Long senderId,
            @RequestParam Long recipientId) {

        System.out.println("Sender ID: " + senderId + ", Recipient ID: " + recipientId);

        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found with ID: " + senderId));

        User receiver = userService.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Receiver not found with ID: " + recipientId));

        List<Notification> messages = notificationService.getMessagesBetweenUsers(sender, receiver);

        List<String> messageContents = messages.stream()
                .map(notification -> {
                    String responsePrefix = notification.getParentId() != null ? "Réponse : " : "";
                    return notification.getSenderName() + " " + responsePrefix + notification.getMessage() + " le " + notification.getTimestamp();
                })
                .toList();

        return ResponseEntity.ok(messageContents);
    }
*/

   /* @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDto messageDto) {
        // Log the incoming MessageDto

        // Get the currently authenticated user
        User currentUser = messageService.getCurrentAuthenticatedUser();
        Long demandeId = messageDto.getDemandeId();

        // Check if demandeId is null
        if (demandeId == null) {
            return ResponseEntity.badRequest().body("Demande ID cannot be null");
        }

        // Get the demande by its ID
        Demande demande = demandeService.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        User sender;
        User receiver;

        // Get the authenticated user's role
        String role = messageService.getAuthenticatedUserRole();

        // Check the role of the current user
        if ("ROLE_USER".equals(role)) {
            // The sender is the currently authenticated user (i.e. the logged-in user)
            sender = currentUser;

            // The receiver is the employee related to the demande
            // The receiver is the user with the ROLE_RES
            Long resUserId = userService.getUserIdByRole("ROLE_RES"); // Retrieve the ID directly

            // Check if a valid user ID was found
            if (resUserId == null) {
                return ResponseEntity.badRequest().body("Receiver user not found or invalid.");
            }

            Optional<User> optionalReceiver = userService.findById(resUserId);
            if (!optionalReceiver.isPresent()) {
                return ResponseEntity.badRequest().body("Receiver user not found.");
            }

            receiver = optionalReceiver.get(); // Get the User from the Optional


        } else if ("ROLE_ADMIN".equals(role) || "ROLE_RES".equals(role)) {
            // If the sender is an admin or res, the current user is the sender
            sender = currentUser;

            // The receiver is the employee related to the demande
            String matricule = demande.getEmployeeMatricule();
            Employee employee = employeService.findByMatriculeEmp(matricule)
                    .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
            receiver = employee.getUser();
        } else {
            return ResponseEntity.badRequest().body("Invalid sender role.");
        }

        // Create the message entity
        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        message.setDemande(demande); // Associate the message with the demande

        String senderName = messageService.getSenderNameById(sender.getId());
        String receiverName = messageService.getReceiverNameById(receiver.getId());

        // Set senderName and receiverName on the message
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);

        // Send the message
        messageService.sendMessage(message); // Pass the Message object to the service

        return ResponseEntity.ok("Message sent successfully!");
    }
*/



  /*  @PostMapping(value = "/send", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> sendMessage(
            @RequestPart(value = "message", required = false) String messageJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        MessageDto messageDto = null;

        // Handle the message if provided
        if (messageJson != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                messageDto = objectMapper.readValue(messageJson, MessageDto.class);
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Invalid message data");
            }
        }

        // Get the currently authenticated user
        User currentUser = messageService.getCurrentAuthenticatedUser();

        // Check if messageDto is present to retrieve demandeId
        Long demandeId = messageDto != null ? messageDto.getDemandeId() : null;

        Demande demande = null; // Declare the demande variable here

        // Check if demandeId is null when messageDto is present
        if (messageDto != null && demandeId == null) {
            return ResponseEntity.badRequest().body("Demande ID cannot be null");
        }

        // If demandeId is provided, get the demande by its ID
        if (demandeId != null) {
            demande = demandeService.findById(demandeId)
                    .orElseThrow(() -> new RuntimeException("Demande not found"));
        }

        User sender = currentUser; // The sender is the currently authenticated user
        User receiver;

        // Get the authenticated user's role
        String role = messageService.getAuthenticatedUserRole();

        // Determine the receiver based on the user's role
        if ("ROLE_USER".equals(role)) {
            // The receiver is the employee related to the demande
            Long resUserId = userService.getUserIdByRole("ROLE_RES");

            // Check if a valid user ID was found
            if (resUserId == null) {
                return ResponseEntity.badRequest().body("Receiver user not found or invalid.");
            }

            Optional<User> optionalReceiver = userService.findById(resUserId);
            if (!optionalReceiver.isPresent()) {
                return ResponseEntity.badRequest().body("Receiver user not found.");
            }

            receiver = optionalReceiver.get(); // Get the User from the Optional

        } else if ("ROLE_ADMIN".equals(role) || "ROLE_RES".equals(role)) {
            // If the sender is an admin or res, the receiver is the employee related to the demande
            if (demande != null) { // Check if demande is present
                String matricule = demande.getEmployeeMatricule();
                Employee employee = employeService.findByMatriculeEmp(matricule)
                        .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
                receiver = employee.getUser();
            } else {
                return ResponseEntity.badRequest().body("Receiver not found without a valid demande.");
            }
        } else {
            return ResponseEntity.badRequest().body("Invalid sender role.");
        }

        // Create the message entity
        Message message = new Message();

        // Only set content if messageDto is present
        if (messageDto != null) {
            message.setContent(messageDto.getContent());
        }

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());

        // Handle file upload if present
        if (file != null && !file.isEmpty()) {
            try {
                // Create a unique file name
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                // Save the file on disk (in a temporary location)
                Path fileStoragePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
                Files.write(fileStoragePath, file.getBytes());

                // Save the file name in the message entity
                message.setAttachmentPath(fileName);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
            }
        }

        // Send the message
        messageService.sendMessage(message);
        return ResponseEntity.ok("Message sent successfully!");
    }
*/


  /*  @PostMapping(value = "/send", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> sendMessage(
            @RequestPart(value = "message", required = false) String messageJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "demandeId") Long demandeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("Received Message JSON: " + messageJson);

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.registerModule(new JavaTimeModule());
        objectMapper1.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Deserialize the message JSON if provided
        MessageDto messageDto = null;
        if (messageJson != null) {
            try {
                messageDto = objectMapper1.readValue(messageJson, MessageDto.class);
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Invalid message data");
            }
        }

        // Retrieve the currently authenticated user
        User currentUser = messageService.getCurrentAuthenticatedUser();
        User sender = currentUser; // The sender is the currently authenticated user
        User receiver;

        // Validate the demandeId to find the associated demande
        Demande demande = demandeService.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        // Get the authenticated user's role
        String role = messageService.getAuthenticatedUserRole();

        // Determine the receiver based on the user's role and the demande
        if ("ROLE_USER".equals(role)) {
            // The receiver is the employee related to the demande
            String matricule = demande.getEmployeeMatricule();
            Employee employee = employeService.findByMatriculeEmp(matricule)
                    .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
            receiver = employee.getUser();
        } else if ("ROLE_ADMIN".equals(role) || "ROLE_RES".equals(role)) {
            String matricule = demande.getEmployeeMatricule();
            Employee employee = employeService.findByMatriculeEmp(matricule)
                    .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
            receiver = employee.getUser();
        } else {
            return ResponseEntity.badRequest().body("Invalid sender role.");
        }

        // Create the message entity
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        message.setDemande(demande);

        // Set the content if available
        if (messageDto != null) {
            message.setContent(messageDto.getContent());
        }

        // Handle file upload if present
        if (file != null && !file.isEmpty()) {
            try {
                // Create a unique file name
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                // Save the file on disk (in a temporary location)
                Path fileStoragePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
                Files.write(fileStoragePath, file.getBytes());

                // Save the file name in the message entity
                message.setAttachmentPath(fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
            }
        }

        // Send the message
        messageService.sendMessage(message);

        // Convert the message entity to MessageDto for response
        MessageDto response = MessageDto.fromEntity(message);

        // Return the message as JSON in the desired format
        return ResponseEntity.ok(response);
    }*/

    @PostMapping(value = "/send", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> sendMessage(
            @RequestPart(value = "message", required = false) String messageJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "demandeId") Long demandeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("Received Message JSON: " + messageJson);

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.registerModule(new JavaTimeModule());
        objectMapper1.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        MessageDto messageDto = null;
        if (messageJson != null) {
            try {
                messageDto = objectMapper1.readValue(messageJson, MessageDto.class);
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Invalid message data");
            }
        }

        User currentUser = messageService.getCurrentAuthenticatedUser();
        User sender = currentUser;
        User receiver;

        Demande demande = demandeService.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        String role = messageService.getAuthenticatedUserRole();

        if ("ROLE_USER".equals(role)) {
            String matricule = demande.getEmployeeMatricule();
            Employee employee = employeService.findByMatriculeEmp(matricule)
                    .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
            receiver = employee.getUser();
        } else if ("ROLE_ADMIN".equals(role) || "ROLE_RES".equals(role)) {
            String matricule = demande.getEmployeeMatricule();
            Employee employee = employeService.findByMatriculeEmp(matricule)
                    .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));
            receiver = employee.getUser();
        } else {
            return ResponseEntity.badRequest().body("Invalid sender role.");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        message.setDemande(demande);

        // Obtenez le nom de l'expéditeur et du destinataire
        String senderName = messageService.getSenderNameById(sender.getId());
        String receiverName = messageService.getReceiverNameById(receiver.getId());

        // Définir le nom de l'expéditeur et du destinataire
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);

        // Définir le contenu du message
        if (messageDto != null) {
            message.setContent(messageDto.getContent());
        }

        // Vérifier si un fichier a été téléchargé
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path fileStoragePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
                Files.write(fileStoragePath, file.getBytes());

                // Définir le chemin de l'attachement
                message.setAttachmentPath(fileName);

                // Stocker le fichier en tant que données binaires dans fileData
                message.setFileData(file.getBytes());

                // Définir le type de contenu
                message.setContentType(file.getContentType()); // <-- Ajoutez cette ligne


            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
            }
        }

        messageService.sendMessage(message);

        MessageDto response = MessageDto.fromEntity(message);

        return ResponseEntity.ok(response);
    }



    @GetMapping("/getFilesByDemandeId/{demandeId}")
    public ResponseEntity<byte[]> getFileDataByDemandeId(@PathVariable Long demandeId) {
        // Récupère les messages associés à demandeId qui ont des données de fichier
        List<Message> messages = messageService.getMessagesByDemandeId(demandeId)
                .stream()
                .filter(msg -> msg.getFileData() != null) // Seuls les messages avec des données de fichier
                .collect(Collectors.toList());

        if (!messages.isEmpty()) {
            Message messageWithFile = messages.get(0);
            byte[] fileData = messageWithFile.getFileData();

            // Détermine le type de contenu en fonction du nom de fichier
            String fileName = messageWithFile.getAttachmentFileName();
            String contentType = determineContentType(fileName); // Méthode auxiliaire pour déterminer le type MIME

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("inline", fileName); // Utilisez "attachment" si vous préférez le téléchargement

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Méthode pour déterminer le type MIME en fonction de l'extension du fichier
    private String determineContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream"; // Type par défaut si le nom du fichier est manquant
        }
        fileName = fileName.toLowerCase(); // Convertit le nom de fichier en minuscules pour éviter les erreurs

        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) return "application/msword";
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "application/vnd.ms-excel";
        // Ajoutez d'autres types si nécessaire

        return "application/octet-stream"; // Type par défaut pour les types inconnus
    }


    // Helper method to determine content type based on file extension

    private final String uploadDir = "C:/Users/HP/Downloads/"; // Directory where files are stored

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Load file from the predefined upload directory
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Set the content disposition header for file download
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(determineContentType(file))  // Detect file type
                        .body(resource);
            } else {
                throw new FileNotFoundException("File not found: " + filename);
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);  // Return 404 if file is not found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // Return 500 for any other error
        }
    }

    // Helper method to determine the content type of the file
    private MediaType determineContentType(Path file) {
        // Here you can implement logic to determine the content type based on file extension
        String contentType = null;

        try {
            contentType = Files.probeContentType(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
    }

    // Utility method to detect content type based on file extension



   /* @GetMapping("/between")
    public ResponseEntity<List<MessageDto>> getMessagesBetweenUser(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            Authentication authentication) {

        // Extract user details and roles
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Check user roles and adapt logic
        boolean isAdminOrRes = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_RES"));

        List<Message> messages;
        if (isAdminOrRes) {
            // Fetch messages where either sender or receiver is ADMIN or RES
            messages = messageService.findMessagesWithAdminOrRes(senderId, receiverId);
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
            // Fetch all messages for ROLE_USER
            messages = messageService.findAllMessagesForUser(senderId, receiverId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Convert to DTOs
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> MessageDto.builder()
                        .id(message.getId())
                        .sender(message.getSender().getId())
                        .receiver(message.getReceiver().getId())
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .senderName(message.getSenderName())
                        .receiverName(message.getReceiverName())
                        .attachmentPath(message.getAttachmentPath())
                        .fileData(message.getFileData())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDtos);
    }
*/
   @GetMapping("/for-demande")
   public ResponseEntity<?> getMessagesForDemande(
           @RequestParam Long demandeId,
           Authentication authentication) {

       UserDetails userDetails = (UserDetails) authentication.getPrincipal();
       Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

       boolean isAdminOrRes = authorities.stream()
               .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_RES"));

       List<Message> messages;

       try {
           // Récupérer les messages en fonction des rôles de l'utilisateur
           if (isAdminOrRes) {
               messages = messageService.findMessagesByDemandeId(demandeId);
           } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
               Long userId = ((User) userDetails).getId();
               messages = messageService.findMessagesForUserByDemandeId(demandeId, userId);
           } else {
               return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
           }

           // Vérifier si la liste des messages est vide
           if (messages == null || messages.isEmpty()) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
           }

           // Vérifiez si le message contient fileData et renvoyez le fichier
           for (Message message : messages) {
               if (message.getFileData() != null) {
                   byte[] fileData = message.getFileData();

                   HttpHeaders headers = new HttpHeaders();
                   String contentType = message.getContentType() != null ? message.getContentType() : "application/octet-stream";
                   headers.setContentType(MediaType.parseMediaType(contentType));
                   headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + message.getAttachmentFileName() + "\"");

                   // Retourner le fichier directement
                   return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
               }
           }

           // Si aucun message n'a de fichier, retournez les messages comme JSON
           List<MessageDto> messageDtos = messages.stream()
                   .map(message -> MessageDto.builder()
                           .id(message.getId())
                           .content(message.getContent())
                           .timestamp(message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now())
                           .isRead(message.isRead())
                           .sender(message.getSender() != null ? message.getSender().getId() : null)
                           .receiver(message.getReceiver() != null ? message.getReceiver().getId() : null)
                           .demandeId(demandeId)
                           .senderName(message.getSenderName() != null ? message.getSenderName() : "Unknown Sender")
                           .receiverName(message.getReceiverName() != null ? message.getReceiverName() : "Unknown Receiver")
                           .attachmentPath(message.getAttachmentPath())
                           .build())
                   .collect(Collectors.toList());

           return ResponseEntity.ok(messageDtos);
       } catch (Exception e) {
           // En cas d'erreur, retourner une réponse d'erreur interne
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }



/*

    @GetMapping("/between")
    public ResponseEntity<List<MessageDto>> getMessagesBetweenUser(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {

        System.out.println("getMessagesBetweenUser method called");

        // Check the authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            System.out.println("Authenticated User: " + authentication.getName());
            System.out.println("User Roles: " + authentication.getAuthorities());
        } else {
            System.out.println("No user is authenticated.");
        }


        // Get the current user's role
        String currentUserRole = userService.getCurrentUserRole(); // Implement this method to get the current user's role
        Long currentUserId = userService.getCurrentUserId(); // Implement this method to get the current user's ID

        System.out.println("Current User ID: " + currentUserId);
        System.out.println("Current User Role: " + currentUserRole);

        // Check if the current user has the role "USER"
        if ("ROLE_USER".equals(currentUserRole)) {
            // Check if the senderId or receiverId matches the current user's ID
            if (!currentUserId.equals(senderId) && !currentUserId.equals(receiverId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
            }
        }

        // Call the service to get the messages
        List<Message> messages = messageService.getConversationBetweenUsers(senderId, receiverId);

        // Convert the Message entities to MessageDto
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> MessageDto.builder()
                        .id(message.getId())
                        .sender(message.getSender().getId())   // Get sender ID
                        .receiver(message.getReceiver().getId()) // Get receiver ID
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .senderName(message.getSenderName())
                        .receiverName(message.getReceiverName())
                        .attachmentPath(message.getAttachmentPath())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDtos);
    }

*/

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<List<MessageDto>> getMessagesByDemandeId(@PathVariable Long demandeId) {
        List<Message> messages = messageService.getMessagesByDemandeId(demandeId);

        // Convert the Message entities to MessageDto
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> MessageDto.fromEntity(message))
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDtos);
    }

    @GetMapping("/receiverId/{demandeId}")
    public ResponseEntity<Long> getReceiverIdByDemandeId(@PathVariable Long demandeId) {
        Long receiverId = messageService.getReceiverIdByDemandeId(demandeId);
        return ResponseEntity.ok(receiverId);
    }

    @GetMapping("/senderId/{demandeId}")
    public ResponseEntity<Long> getSenderIdByDemandeId(@PathVariable Long demandeId) {
        Long senderId = Long.valueOf(messageService.getSenderNameById(demandeId));
        return ResponseEntity.ok(senderId);
    }





    /*  @PostMapping("/sendFile")
    public ResponseEntity<?> sendMessageWithAttachment(
            @RequestParam("message") String messageDtoJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException{

        // Convertir le JSON messageDto en objet MessageDto
        ObjectMapper objectMapper = new ObjectMapper();
        MessageDto messageDto = objectMapper.readValue(messageDtoJson, MessageDto.class);

        // Get the currently authenticated user
        User currentUser = messageService.getCurrentAuthenticatedUser();
        Long demandeId = messageDto.getDemandeId();

        if (demandeId == null) {
            return ResponseEntity.badRequest().body("Demande ID cannot be null");
        }

        Demande demande = demandeService.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        // Get employee from demande
        String matricule = demande.getEmployeeMatricule();
        Employee employee = employeService.findByMatriculeEmp(matricule)
                .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));

        // Get authenticated user's role
        String role = messageService.getAuthenticatedUserRole();
        User sender;
        User receiver;

        if ("ROLE_USER".equals(role)) {
            sender = employee.getUser();
            receiver = userService.findAnyAdminOrRes()
                    .orElseThrow(() -> new RuntimeException("No user found with role ROLE_ADMIN or ROLE_RES"));
        } else if ("ROLE_ADMIN".equals(role) || "ROLE_RES".equals(role)) {
            sender = currentUser;
            receiver = employee.getUser();
        } else {
            return ResponseEntity.badRequest().body("Invalid sender role.");
        }

        // Generate a dynamic upload directory (e.g., using a temp directory)
        String uploadDir = System.getProperty("java.io.tmpdir"); // or any preferred directory
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // Create the message with the attachment
        Message message = new Message();
        message.setContent(messageDto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        message.setDemande(demande);
        message.setAttachmentPath(filePath.toString());

        String senderName = messageService.getSenderNameById(sender.getId());
        String receiverName = messageService.getReceiverNameById(receiver.getId());

        // Set senderName and receiverName on the message
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);

        messageService.sendMessage(message);

        return ResponseEntity.ok("Message with attachment sent successfully!");
    }

*/
    // Sanitize the user input path to prevent directory traversal attacks
    private String sanitizePath(String uploadPath) {
        // Implement sanitization logic to prevent directory traversal attacks
        // This regex allows alphanumeric characters, underscores, dots, and slashes
        return uploadPath.replaceAll("[^a-zA-Z0-9_./-]", ""); // Allow only safe characters
    }




}