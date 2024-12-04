package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.ObjectsValidator;
import com.snim.demandesrh.config.JwtUtils;
import com.snim.demandesrh.config.TokenBlacklistServicee;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.Role;
import com.snim.demandesrh.entities.Token;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.AuthenticationRequest;
import com.snim.demandesrh.entities.dto.AuthenticationResponse;
import com.snim.demandesrh.entities.dto.LightUserDto;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.enums.TokenType;
import com.snim.demandesrh.exceptions.AccountDisabledException;
import com.snim.demandesrh.exceptions.UserNotFoundException;
import com.snim.demandesrh.exceptions.UserUpdateException;
import com.snim.demandesrh.repository.RoleRepository;
import com.snim.demandesrh.repository.TokenRepository;
import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.IUserServices;
import com.snim.demandesrh.service.auth.EmailVerificationService;
import com.snim.demandesrh.service.auth.TokenBlacklistService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;


import java.util.Optional;
import com.snim.demandesrh.exceptions.RoleNotFoundException;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserServices {

    @Autowired
    private JwtUtils jwtutils;
    private static final String ROLE_USER = "ROLE_USER";
    private final UserRepository repository;
    private final ObjectsValidator<UserDto> validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final TokenBlacklistServicee tokenBlacklistServicee;
    private int lastAssignedIndex = -1;

    @Override
    public long save(UserDto dto) {
        validator.validate(dto);
        User user = UserDto.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Math.toIntExact(repository.save(user).getId());
    }


    @Override
    @Transactional
    public List<UserDto> findAll() {
        return repository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        // Fetch the user by userId
        User user = repository.findById(userId.intValue())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch the role by roleName
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Set the role to the user
        user.setRole(role);

        // Save the updated user
        repository.save(user);
    }


    @Override
    public UserDto findById(Integer id) {
        return repository.findById(id)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("No user was found with the provided ID : " + id));
    }

    @Override
    public void delete(Integer id) {
        // todo check before delete
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public Integer validateAccount(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user was found for user account validation"));


        user.setActive(true);
        repository.save(user);
        return Math.toIntExact(user.getId());
    }

    @Override
    public Integer invalidateAccount(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user was found for user account validation"));

        user.setActive(false);
        repository.save(user);
        return Math.toIntExact(user.getId());
    }


    // Autres méthodes

    @Override
    public AuthenticationResponse register(UserDto userDto) {
        // Validate email address
        if (!emailVerificationService.verifyEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // Encrypt the password before saving the user
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        // Create and save the user
        User user = UserDto.toEntity(userDto);
        user.setPassword(encodedPassword);  // Use the encrypted password
        user.setCreatedAt(LocalDateTime.now()); // Set the creation timestamp
        user.setActive(true); // Set default active status

        // Set default role to ROLE_USER if not provided
        String roleName = userDto.getRole() != null ? userDto.getRole() : "ROLE_USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role provided"));

        user.setRole(role);
        repository.save(user);

        // Generate JWT token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role.getName())) // Set the role authority
        );

        String token = jwtUtils.generateToken(userDetails);

        // Create and return authentication response
        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken(token);
        response.setRole(role.getName()); // Return the actual role name
        response.setMessage("User registered successfully");

        return response;
    }


    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser((int) user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
//
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Optional<User> optionalUser = repository.findByEmail(request.getEmail());

        // Vérifiez si l'utilisateur est présent
        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            if (user.getDeactivationDate() != null &&
                    Duration.between(user.getDeactivationDate(), LocalDateTime.now()).toDays() >= 7) {
                user.setActive(true);
                user.setDeactivationDate(null);
                repository.save(user);
            } else {
                throw new AccountDisabledException("Votre compte est désactivé.");
            }
        }

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("fullName", user.getName());
            claims.put("role", user.getRole() != null ? user.getRole().getName() : "UNKNOWN");

            final String token = jwtUtils.generateToken(user, claims);
            saveUserToken(user, token);

            return AuthenticationResponse.builder()
                    .token(token)
                    .role(user.getRole() != null ? user.getRole().getName() : "UNKNOWN")
                    .message("Authentication successful")
                    .build();
        } else {
            throw new RuntimeException("Authentication failed");
        }
    }






    @Override
    public Integer update(LightUserDto userDto) {
        User user = LightUserDto.toEntity(userDto);
        return Math.toIntExact(repository.save(user).getId());
    }
    private Role findOrCreateRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElse(null);
        if (role == null) {
            return roleRepository.save(
                    Role.builder()
                            .name(roleName)
                            .build()
            );
        }
        return role;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }




    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email){ return repository.findByEmail(email);}

    public String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifiez si l'authentification est nulle
        if (authentication == null) {
            System.out.println("Authentication is null");
            return null;
        }

        // Récupérez l'objet principal
        Object principal = authentication.getPrincipal();

        // Log pour vérifier le type de principal
        System.out.println("Principal class: " + principal.getClass().getName());

        // Assurez-vous que le principal est un UserDto et récupérez l'email si possible
        if (principal instanceof UserDto) {
            UserDto userDto = (UserDto) principal;
            System.out.println("Email retrieved: " + userDto.getEmail());
            return userDto.getEmail();
        }

        // Si le principal n'est pas un UserDto, loggez cette information
        System.out.println("Principal is not an instance of UserDto");
        return null;
    }



    public UserDto getCurrentUser() {
        String email = getCurrentEmail();
        return findByEmail(email)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();  // Récupère l'email de l'utilisateur authentifié

        // Utilisez le service pour obtenir l'utilisateur avec les rôles initialisés
        User user = getUserWithRoles(email);

        // Vérifier si le mot de passe actuel correspond
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserWithRoles(String email) {
        return repository.findByEmailWithRole(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur avec cet email non trouvé"));
    }




    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = repository.findByEmail(username);
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return userOptional.get(); // Retourner l'entité User qui implémente UserDetails
    }

    public UserDto loadUserDtoByEmail(String email) {
        Optional<User> userOptional = repository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return UserDto.fromEntity(userOptional.get());
    }

    public void associateEmployeeToUser(Integer userId, Employee employee) {
        User user = repository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        user.setEmployee(employee);
        employee.setUser(user);
        repository.save(user);
    }
    //am using
    @Transactional
    public void changeUserRoleByMatricule(String matricule, String newRole) {
        // Log des rôles disponibles
        List<Role> allRoles = roleRepository.findAll(); // Vous devrez peut-être créer cette méthode
        System.out.println("Available roles: " + allRoles.stream().map(Role::getName).collect(Collectors.toList()));

        User user = repository.findByMatricule(matricule) // Assuming you have this method in your repository
                .orElseThrow(() -> new UserNotFoundException("User not found with matricule: " + matricule));

        Optional<Role> roleOptional = roleRepository.findByName(newRole);
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            user.setRole(role);
            repository.save(user);
            System.out.println("User role updated successfully.");
        } else {
            throw new RoleNotFoundException("Role not found: " + newRole);
        }
    }



    @Transactional
    public void toggleUserAccountByMatricule(String matricule, boolean activate) {
        // Récupère l'utilisateur en fonction du matricule
        User user = repository.findByMatricule(matricule) // Assuming you have this method in your repository
                .orElseThrow(() -> new UserNotFoundException("User not found with matricule: " + matricule));

        // Active ou désactive le compte de l'utilisateur
        user.setActive(activate);

        // Enregistre la date de désactivation uniquement si le compte est désactivé
        if (!activate) {
            user.setDeactivationDate(LocalDateTime.now());
        } else {
            user.setDeactivationDate(null); // Réinitialise la date si le compte est activé
        }

        // Enregistre la modification dans la base de données
        repository.save(user);

        String action = activate ? "activated" : "deactivated";
        System.out.println("User account " + action + " successfully.");
    }


    public void changeUserPhoto(String username, MultipartFile photo) throws IOException {
        Optional<User> userOptional = repository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Save the photo as a base64 string
            user.setPhoto(Base64.getEncoder().encodeToString(photo.getBytes()));
            repository.save(user);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }



    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        String username = authentication.getName();
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getId();
    }




    public List<UserDto> getAdmins() {
        List<User> admins = repository.findByRoleName("ROLE_ADMIN");
        return admins.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }


    public Optional<User> findById(Long id) {
        return repository.findById(id.intValue());
    }

    public List<UserDto> getRes() {
        List<User> res = repository.findByRoleName("ROLE_RES");
        return res.stream()
                .map(user -> new UserDto(user.getId(), null, null, null, null, null, null, null, null,false, null, null)) // Create UserDto with only ID
                .collect(Collectors.toList());
    }
    public Long getUserIdByRole(String role) {
        List<User> users = repository.findByRoleName(role);
        return !users.isEmpty() ? users.get(0).getId() : null; // Return the ID of the first user or null if not found
    }

    public String getCurrentUserRole() {
        // Implement your logic to get the currently authenticated user's role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

   /* private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Assuming you have a UserService that returns Optional<User>
            Optional<User> userOptional = findByUsername(userDetails.getUsername());

            // Safely retrieve the user ID or throw an exception if not found
            return userOptional.map(User::getId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        return null;
    }*/





    @Transactional
    public void deactivateUserAccount(Long userId) {
        // Récupère l'utilisateur en fonction de l'ID
        User user = repository.findById(userId.intValue())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Désactive le compte de l'utilisateur
        user.setActive(false);
        user.setDeactivationDate(LocalDateTime.now()); // Enregistre la date de désactivation

        // Enregistre la modification dans la base de données
        repository.save(user);

        System.out.println("User account deactivated successfully.");
    }

    @Transactional
    public void toggleUserAccount(Long userId, boolean activate) {
        // Récupère l'utilisateur en fonction de l'ID
        User user = repository.findById(userId.intValue())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Active ou désactive le compte de l'utilisateur
        user.setActive(activate);

        // Enregistre la date de désactivation uniquement si le compte est désactivé
        if (!activate) {
            user.setDeactivationDate(LocalDateTime.now());
        } else {
            user.setDeactivationDate(null); // Réinitialise la date si le compte est activé
        }

        // Enregistre la modification dans la base de données
        repository.save(user);

        String action = activate ? "activated" : "deactivated";
        System.out.println("User account " + action + " successfully.");
    }


    public Long findUserIdByMatricule(String matricule) {
        return repository.findUserIdByMatricule(matricule);
    }

    public String getMatriculeFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Supposons que votre UserDetails a une méthode getMatricule
            // Remplacez cela par la méthode correcte pour obtenir le matricule
            return ((User) userDetails).getMatricule();
        }
        return null;
    }


    public UserDto updateUserRole(Long userId, String role) {
        try {
            User user = repository.findById(userId.intValue())
                    .orElseThrow(() -> new UserUpdateException("User not found with ID: " + userId));

            user.setRole(new Role(role)); // Mettez à jour le rôle
            repository.save(user);

            return UserDto.fromEntity(user); // Convertir l'entité en DTO
        } catch (UserUpdateException e) {
            // Vous pouvez également journaliser l'erreur ici
            throw e; // Propager l'exception pour la gestion ultérieure
        } catch (Exception e) {
            // Gérer les autres exceptions
            throw new UserUpdateException("Error updating user role: " + e.getMessage());
        }
    }

    public UserDto updateUserStatus(Long userId, boolean active) {
        User user = repository.findById(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setActive(active); // Mettez à jour l'état
        repository.save(user);

        return UserDto.fromEntity(user); // Convertir l'entité en DTO
    }
    public UserDto updateUser(Long userId, String role, boolean active) {
        User user = repository.findById(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (role != null) {
            user.setRole(new Role(role)); // Mettez à jour le rôle
        }
        user.setActive(active); // Mettez à jour l'état
        repository.save(user);

        return UserDto.fromEntity(user); // Convertir l'entité en DTO
    }

    public String getUsernameById(Long userId) {
        return repository.findById(userId.intValue())
                .map(User::getUsername)
                .orElse("Unknown"); // Return "Unknown" if user not found
    }

    /*public void logout(String token) {
        // Ensure that the token is blacklisted
        jwtUtils.blacklistToken(token);

        // Clear the security context to log out the user
        SecurityContextHolder.clearContext();
    }*/
    public boolean isUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }







}

