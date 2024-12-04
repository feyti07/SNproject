package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Message;
import com.snim.demandesrh.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    List<Message> findBySenderAndReceiver(User sender, User receiver);
    List<Message> findByReceiver(User receiver);

    // Optionally, you can add a method to get unread messages
    List<Message> findByReceiverAndIsReadFalse(User receiver);
    List<Message> findByDemandeId(Long demandeId);


    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)")
    List<Message> findConversationBetweenUsers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT m.sender.username FROM Message m WHERE m.id = :messageId")
    String findSenderNameByMessageId(@Param("messageId") Long messageId);

    @Query("SELECT m.receiver.username FROM Message m WHERE m.id = :messageId")
    String findReceiverNameByMessageId(@Param("messageId") Long messageId);

    // Dans MessageRepository (si vous utilisez JPA ou Hibernate)


    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) " +
            "OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)")
    List<Message> findMessagesBetweenUsers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId OR m.receiver.id = :receiverId) AND (m.sender.isAdmin = true OR m.receiver.isAdmin = true)")
    List<Message> findMessagesWhereSenderOrReceiverIsAdminOrRes(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.demande.id = :demandeId")
    List<Message> findByDemandeId2(@Param("demandeId") Long demandeId);
    @Query("SELECT m FROM Message m WHERE m.demande.id = :demandeId AND (m.sender.id = :userId OR m.receiver.id = :userId)")
    List<Message> findByDemandeIdAndUser(@Param("demandeId") Long demandeId, @Param("userId") Long userId);


}
