package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
public class TokenBlacklist{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Date expiryDate;

    public TokenBlacklist(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }


}
