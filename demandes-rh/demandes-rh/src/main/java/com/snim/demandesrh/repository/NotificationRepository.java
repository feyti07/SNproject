package com.snim.demandesrh.repository;
import com.snim.demandesrh.entities.Notification;
import com.snim.demandesrh.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireAndLueFalse(String destinataire);


}