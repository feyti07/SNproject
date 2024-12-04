package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;
    public String libelle;
    public String description;


    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reclamation> reclamations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "category", orphanRemoval = true)
    private Set<Demande> demandes = new LinkedHashSet<>();

}
