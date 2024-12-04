package com.snim.demandesrh.config;

import com.snim.demandesrh.entities.TokenBlacklist;
import com.snim.demandesrh.repository.TokenBlacklistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenBlacklistServicee {
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    public TokenBlacklistServicee(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public void addTokenToBlacklist(String token) {
        // Définir la date d'expiration (par exemple, 30 minutes à partir de maintenant)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30); // Vous pouvez changer cela en fonction de vos besoins
        Date expiryDate = calendar.getTime();

        TokenBlacklist tokenBlacklist = new TokenBlacklist(token, expiryDate);
        tokenBlacklistRepository.save(tokenBlacklist);
    }

    // Vérifier si le token est dans la liste noire
    @Transactional
    public boolean contains(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
    @Transactional
    public void removeExpiredTokens() {
        Date now = new Date();
        tokenBlacklistRepository.deleteByExpiryDateBefore(now);
    }
}