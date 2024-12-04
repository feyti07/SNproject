package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Role;
import com.snim.demandesrh.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String matricule;
    private String uoCode;
    private String uoText;
    private String positionCode;
    private String positionText;
    private boolean active;
    private String role;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .username(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .matricule(user.getMatricule())
                .uoCode(user.getUoCode())
                .uoText(user.getUoText())
                .positionCode(user.getPositionCode())
                .positionText(user.getPositionText())
                .active(user.isActive())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static User toEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setMatricule(dto.getMatricule());
        user.setUoCode(dto.getUoCode());
        user.setUoText(dto.getUoText());
        user.setPositionCode(dto.getPositionCode());
        user.setPositionText(dto.getPositionText());
        user.setActive(dto.isActive());
        // Assuming Role constructor takes a role name
        user.setRole(new Role(dto.getRole()));
        return user;
    }
}
