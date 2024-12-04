package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Reclamation;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.enums.ReclamationEnums.*;
import com.snim.demandesrh.enums.ImpactEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.enums.TypeEnum;
import com.snim.demandesrh.enums.UrgenceEnum;
import lombok.*;
import com.snim.demandesrh.enums.DemandeEnums.CentreEnum;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReclamationDto {

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

    public static ReclamationDto fromEntity(Reclamation reclamation) {
        return ReclamationDto.builder()
                .id(reclamation.getId())
                .center(reclamation.getCenter())
                .lieu(reclamation.getLieu())
                .categorie(reclamation.getCategorie())
                .urgence(reclamation.getUrgence())
                .impact(reclamation.getImpact())
                .employeeMatricule(reclamation.getEmployee() != null ? reclamation.getEmployee().getMatricule() : null)
                .description(reclamation.getDescription())
                .pieceJointe(reclamation.getPieceJointe())
                .type(reclamation.getType())
                .status(reclamation.getStatus())
                .createdAt(reclamation.getCreatedAt())
                .updatedAt(reclamation.getUpdatedAt())
                .updatedBy(reclamation.getUpdatedBy())
                .createur(reclamation.getCreateur())
                .source(reclamation.getSource())
                .build();
    }

    public static Reclamation toEntity(ReclamationDto dto, Employee employee) {
        return Reclamation.builder()
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
