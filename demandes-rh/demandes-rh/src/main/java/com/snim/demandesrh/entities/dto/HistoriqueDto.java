package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Historique;
import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Intervenant;
import com.snim.demandesrh.entities.Reclamation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoriqueDto {

    private Long id;
    private UUID uuid;
    private String oldValue;
    private String newValue;
    private String champs; // intervenant || observateur || statut
    private LocalDateTime date;
    private String description;
    private String type;
    private Long reclamationId;
    private Long demandeId;
    private String acteur; // Change from Long to String

    public static HistoriqueDto fromEntity(Historique historique) {
        if (historique == null) {
            return null;
        }

        return HistoriqueDto.builder()
                .id(historique.getId())
                .uuid(historique.getUuid())
                .date(historique.getDate())
                .description(historique.getDescription())
                .type(historique.getType())
                .reclamationId(historique.getReclamation() != null ? historique.getReclamation().getId() : null)
                .demandeId(historique.getDemande() != null ? historique.getDemande().getId() : null)
                .acteur(historique.getActeur()) // acteur is a String now
                .build();
    }

    public static Historique toEntity(HistoriqueDto dto, Reclamation reclamation, Demande demande) {
        if (dto == null) {
            return null;
        }

        return Historique.builder()
                .id(dto.getId())
                .uuid(dto.getUuid())
                .date(dto.getDate())
                .description(dto.getDescription())
                .type(dto.getType())
                .reclamation(reclamation)
                .demande(demande)
                .acteur(dto.getActeur()) // acteur is a String here
                .build();
    }
}
