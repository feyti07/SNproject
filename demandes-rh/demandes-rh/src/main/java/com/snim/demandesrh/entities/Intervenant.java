package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity

public class Intervenant extends BaseUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToOne(mappedBy = "intervenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Demande demande;

    @OneToOne(mappedBy = "intervenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Reclamation reclamation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "groupee_id")
    private Groupe groupee;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "employee_id")
    private Employee employee;



}
