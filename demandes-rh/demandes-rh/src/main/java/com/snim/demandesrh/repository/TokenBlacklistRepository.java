package com.snim.demandesrh.repository;
import com.snim.demandesrh.entities.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByToken(String token);
    void deleteByExpiryDateBefore(Date now);
}