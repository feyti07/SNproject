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

public class Observateur extends BaseUser{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    private Demande demande;


    @ManyToOne
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "employee_id")
    private Employee employee;


}
