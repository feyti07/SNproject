package com.snim.demandesrh.service.auth;

import com.snim.demandesrh.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository repository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No user was found with the provided email"))
                ;
    }
    public String getMatriculeFromPrincipal(UserDetails userDetails) {
        // Assuming matricule is stored in username
        return userDetails.getUsername();
    }
}
