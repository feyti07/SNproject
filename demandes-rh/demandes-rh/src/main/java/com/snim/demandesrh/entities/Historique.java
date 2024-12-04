package com.snim.demandesrh.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Historique extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    private UUID uuid;
    private LocalDateTime date;
    private String description;
    private String type;
    private String acteurName;



    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;

    @ManyToOne
    @JoinColumn(name = "demande_id")
    @JsonIgnore
    private Demande demande;

    private String acteur;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Historique that = (Historique) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
