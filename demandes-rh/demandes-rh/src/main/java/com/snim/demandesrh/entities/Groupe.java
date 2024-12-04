package com.snim.demandesrh.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
public class Groupe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;
    public String nom;

    /*@OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Intervenant> intervenants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "groupe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Observateur> observateurs = new LinkedHashSet<>();*/

}
