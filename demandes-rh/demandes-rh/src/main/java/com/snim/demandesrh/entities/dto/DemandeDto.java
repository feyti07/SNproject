package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.enums.DemandeEnums.*;
import com.snim.demandesrh.enums.ImpactEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.enums.TypeEnum;
import com.snim.demandesrh.enums.UrgenceEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemandeDto {

    private long id;
    private CentreEnum center;
    private LieuEnum lieu;
    private CategorieEnum categorie;
    private UrgenceEnum urgence;
    private ImpactEnum impact;
    private String employeeMatricule;
    public String description;
    private byte[] pieceJointe;
    private TypeEnum type;
    private StatutEnum status;
    private LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    private String createur;
    public String updatedBy;
    private String source;




    public static DemandeDto fromEntity(Demande demande) {
        return DemandeDto.builder()
                .id(demande.getId())
                .center(demande.getCenter())
                .lieu(demande.getLieu())
                .categorie(demande.getCategorie())
                .urgence(demande.getUrgence())
                .impact(demande.getImpact())
                .employeeMatricule(demande.getEmployee() != null ? demande.getEmployee().getMatricule() : null)
                .description(demande.getDescription())
                .pieceJointe(demande.getPieceJointe())
                .type(demande.getType())
                .status(demande.getStatus())
                .createdAt(demande.getCreatedAt())
                .updatedAt(demande.getUpdatedAt())
                .updatedBy(demande.getUpdatedBy())
                .createur(demande.getCreateur())
                .source(demande.getSource())
                .build();
    }

    public static Demande toEntity(DemandeDto dto, Employee employee) {
        return Demande.builder()
                .id(dto.getId())
                .center(dto.getCenter())
                .lieu(dto.getLieu())
                .categorie(dto.getCategorie())
                .urgence(dto.getUrgence())
                .impact(dto.getImpact())
                .employee(employee)
                .description(dto.getDescription())
                .type(dto.getType())
                .status(dto.getStatus())
                .createur(dto.getCreateur())
                .source(dto.getSource())
                .build();
    }
}
