package com.snim.demandesrh.service.auth;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TokenBlacklistService {
    private final Set<String> blacklist = new HashSet<>();

    public boolean contains(String token) {
        return blacklist.contains(token);
    }
}
