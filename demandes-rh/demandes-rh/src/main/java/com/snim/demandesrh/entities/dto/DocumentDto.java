package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Document;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentDto {

    private long id;
    private UUID uuid;              // UUID du document
    private String fileName;        // Nom du fichier
    private String fileLocation;     // Emplacement du fichier (si nécessaire)
    private byte[] fileData;        // Données du fichier (en tant que tableau d'octets)

    // Si vous avez besoin de références à d'autres entités, vous pouvez les ajouter ici.
    // Par exemple, si vous souhaitez inclure l'ID de la demande associée :
    private Long demandeId;         // ID de la demande associée (si applicable)
    private Long reclamationId;

    private String contentType;// ID de la réclamation associée (si applicable)

    // Méthode pour convertir l'entité Document en DocumentDto
    public static DocumentDto fromEntity(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .uuid(document.getUuid())
                .fileName(document.getFileName())
                .fileLocation(document.getFileLocation())
                .fileData(document.getFileData())
                .contentType(document.getContentType())
                .demandeId(document.getDemande() != null ? document.getDemande().getId() : null)  // Récupérer l'ID de la demande
                .reclamationId(document.getReclamation() != null ? document.getReclamation().getId() : null) // Récupérer l'ID de la réclamation
                .build();
    }

    // Méthode pour convertir DocumentDto en Document
    public static Document toEntity(DocumentDto dto) {
        Document document = new Document();
        document.setId(dto.getId());
        document.setUuid(dto.getUuid());
        document.setFileName(dto.getFileName());
        document.setFileLocation(dto.getFileLocation());
        document.setFileData(dto.getFileData());
        document.setContentType(dto.getContentType());

        // Vous pouvez définir des relations ici si nécessaire, par exemple :
        // document.setDemande(new Demande(dto.getDemandeId())); // Si vous avez l'ID de la demande
        // document.setReclamation(new Reclamation(dto.getReclamationId())); // Si vous avez l'ID de la réclamation

        return document;
    }
}
