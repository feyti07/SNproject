package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.EmailConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, String>  {
    EmailConfirmationToken findByToken(String token);
}
