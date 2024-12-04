package com.snim.demandesrh.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Builder
@Table(name = "User_")
public class User extends BaseUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private UUID uuid;
    private LocalDateTime deactivationDate;
    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Token> tokens;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Employee employee;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String username;

    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private boolean active;

    private boolean isAdmin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getName() {
        return username;
    }

    @OneToMany(mappedBy = "receiver")
    private List<ChatMessage> receivedMessages;

    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> sendedMessages;

    @Override
    public int hashCode() {
        return Objects.hash(id, email, username); // Avoid including employee in hashCode
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", matricule='" + matricule + '\'' +
                ", active=" + active +
                '}';  // Ne pas inclure "role" et "tokens" ici
    }

}
