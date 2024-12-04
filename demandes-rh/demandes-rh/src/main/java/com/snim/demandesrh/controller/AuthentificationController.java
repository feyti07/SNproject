package com.snim.demandesrh.controller;
import com.snim.demandesrh.config.JwtUtils;
import com.snim.demandesrh.config.TokenBlacklistServicee;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.AuthenticationRequest;
import com.snim.demandesrh.entities.dto.AuthenticationResponse;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.service.auth.TokenBlacklistService;
import com.snim.demandesrh.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthentificationController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    private TokenBlacklistServicee tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto user) {
        try {
            AuthenticationResponse response = userService.register(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Optional: Helper method to validate roles
    private boolean isValidRole(String role) {
        return role.equals("ROLE_USER") || role.equals("ROLE_ADMIN") || role.equals("ROLE_RES");
    }


    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = userService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtUtils.resolveToken(request);  // Extraire le token de la requête
        if (token != null) {
            tokenBlacklistService.addTokenToBlacklist(token);  // Ajouter le token à la liste noire
        }
        return ResponseEntity.ok().body("Logged out successfully");
    }





    /*@PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(7); // Removes "Bearer " prefix

        // Blacklist the token
        jwtUtils.blacklistToken(token);

        // Clear the security context
        SecurityContextHolder.clearContext();

        // Return a success response
        return ResponseEntity.ok("Logout successful");
    }*/
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Date getTokenExpiryDate(String token) {
        // Logic to extract expiry date from token (e.g., using JWT parser)
        return new Date(System.currentTimeMillis() + 1000 * 60 * 60); // Example expiry date 1 hour later
    }


    @GetMapping("/current-user")
    public ResponseEntity<String> getCurrentUserEmail() {
        String email = userService.getCurrentEmail();
        if (email != null) {
            return ResponseEntity.ok(email);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found" + email);
        }
    }




}






