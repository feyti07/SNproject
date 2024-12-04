package com.snim.demandesrh.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.UserStatusRequest;
import com.snim.demandesrh.entities.dto.PasswordChangeRequest;
import com.snim.demandesrh.entities.dto.ToggleUserActiveStatusRequest;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.service.auth.EmailVerificationService;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String email = userDetails.getUsername();
                Optional<User> optionalUser = userService.findByEmail(email);

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    Map<String, String> response = new HashMap<>();
                    response.put("name", user.getName());
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No authentication"));
        }
    }





    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        userService.changePassword(passwordChangeRequest.getOldPassword(), passwordChangeRequest.getNewPassword());

        // Create a response map
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mot de passe changé avec succès");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/info")
    public ResponseEntity<UserDto> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserDto userDto = userService.loadUserDtoByEmail(email);
        return ResponseEntity.ok(userDto);

    }

    @PostMapping("/{userId}/employee")
    public ResponseEntity<Void> associateEmployeeToUser(@PathVariable Integer userId, @RequestBody Employee employee) {
        userService.associateEmployeeToUser(userId, employee);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    //am using

    @PostMapping("/{matricule}/change-role")
    public ResponseEntity<?> changeUserRole(@PathVariable String matricule, @RequestBody Map<String, String> requestBody) {
        String newRole = requestBody.get("newRole");
        userService.changeUserRoleByMatricule(matricule, newRole);
        return ResponseEntity.ok("User role changed successfully");
    }


    @PutMapping("/{matricule}/active")
    public ResponseEntity<String> toggleUserStatus(@PathVariable String matricule, @RequestBody UserStatusRequest request) {
        userService.toggleUserAccountByMatricule(matricule, request.isActive());
        return ResponseEntity.ok("User account " + (request.isActive() ? "activated" : "deactivated") + " successfully.");
    }


    @PostMapping("/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @RequestBody String roleName) {
        userService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok("Role assigned successfully");
    }



    @PostMapping("/photo")
    public ResponseEntity<?> uploadUserPhoto(@RequestParam("photo") MultipartFile photo) {
        String username = userService.getCurrentUsername();
        try {
            userService.changeUserPhoto(username, photo);
            return ResponseEntity.ok("Photo uploaded successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading photo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<Boolean> verifyEmail(@RequestParam String email) {
        boolean isValid = emailVerificationService.verifyEmail(email);
        return ResponseEntity.ok(isValid);

    }


    @GetMapping("/current-id")
    public ResponseEntity<?> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = ((User) userDetails).getId(); // Assurez-vous que votre UserDetails implémente getId()
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
    }


    @GetMapping("/role")
    public ResponseEntity<String> getCurrentUserRole() {
        String role = userService.getCurrentUserRole();
        if (role != null) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No role found for the current user.");
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserDto>> getAdmins() {
        List<UserDto> admins = userService.getAdmins();
        return ResponseEntity.ok(admins);
    }



    @GetMapping("/res")
    public ResponseEntity<Long> getResUserId() {
        Long resUserId = userService.getUserIdByRole("ROLE_RES");
        if (resUserId != null) {
            return ResponseEntity.ok(resUserId);
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if no user found
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }



    @GetMapping("/mat/{matricule}")
    public ResponseEntity<Long> getUserIdByMatricule(@PathVariable String matricule) {
        Long userId = userService.findUserIdByMatricule(matricule);
        if (userId != null) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        UserDto updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }

    // Met à jour l'état d'un utilisateur (actif ou non)
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        UserDto updatedUser = userService.updateUserStatus(userId, active);
        return ResponseEntity.ok(updatedUser);
    }

    // Met à jour à la fois le rôle et l'état d'un utilisateur
    @PutMapping("/{userId}/both")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        UserDto updatedUser = userService.updateUser(userId, request.getRole(), request.isActive());
        return ResponseEntity.ok(updatedUser);
    }
    public class UserUpdateRequest {
        private String role;
        private boolean active;

        // Getter pour le rôle
        public String getRole() {
            return role;
        }

        // Setter pour le rôle
        public void setRole(String role) {
            this.role = role;
        }

        // Getter pour l'état actif
        public boolean isActive() {
            return active;
        }

        // Setter pour l'état actif
        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @GetMapping("/check-role")
    public boolean checkIfUserIsAdmin() {
        return userService.isUserAdmin();
    }

}