package com.snim.demandesrh.service.impl;
import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Document;
import com.snim.demandesrh.entities.Reclamation;
import com.snim.demandesrh.exceptions.ResourceNotFoundException;
import com.snim.demandesrh.repository.DemandeRepository;
import com.snim.demandesrh.repository.DocumentRepository;
import com.snim.demandesrh.repository.ReclamationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DemandeRepository demandeRepository;

    public DocumentService(DocumentRepository documentRepository, DemandeRepository demandeRepository) {
        this.documentRepository = documentRepository;
        this.demandeRepository = demandeRepository;
    }
    @Transactional
    public Document addDocumentAsPieceJointe(Long demandeId, Document document) {
        Optional<Demande> optionalDemande = demandeRepository.findById(demandeId.intValue());
        if (optionalDemande.isPresent()) {
            Demande demande = optionalDemande.get();
            document.setDemande(demande);
            if (demande.getPiecesJointes() == null) {
                demande.setPiecesJointes(new ArrayList<>());
            }
            demande.getPiecesJointes().add(document); // Ajouter le document à la liste des pièces jointes
            documentRepository.save(document); // Enregistrer le document
            demandeRepository.save(demande); // Mettre à jour la demande
            return document;
        } else {
            throw new RuntimeException("Demande introuvable avec ID: " + demandeId);
        }
    }

    @Autowired
    public ReclamationRepository reclamationRepository;

    @Transactional
    public Document addDocumentAsPJ(Long reclamationId, Document document) {
        Optional<Reclamation> optionalReclamation = reclamationRepository.findById(reclamationId.intValue());
        if (optionalReclamation.isPresent()) {
            Reclamation reclamation = optionalReclamation.get();
            document.setReclamation(reclamation); // Associer le document à la réclamation
            if (reclamation.getPiecesJointes() == null) {
                reclamation.setPiecesJointes(new ArrayList<>()); // Initialiser la liste si nécessaire
            }
            reclamation.getPiecesJointes().add(document); // Ajouter le document à la liste des pièces jointes
            documentRepository.save(document); // Enregistrer le document
            reclamationRepository.save(reclamation); // Mettre à jour la réclamation
            return document;
        } else {
            throw new RuntimeException("Réclamation introuvable avec ID: " + reclamationId);
        }
    }




    public Document findByDemandeIdAndFileName(Long demandeId, String fileName) {
        // Your logic to find the document by demandeId and fileName
        return documentRepository.findByDemandeIdAndFileName(demandeId, fileName);
    }
    public List<Document> findAllByDemandeId(Long demandeId) {
        return documentRepository.findByDemandeId(demandeId);
    }

    public ResponseEntity<Resource> downloadFile(Long id) throws Exception {
        Document document = documentRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Load file from its location
        File file = new File(document.getFileLocation());

        if (!file.exists()) {
            throw new RuntimeException("File not found at location: " + document.getFileLocation());
        }

        // Prepare file as a resource for download
        FileSystemResource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id.intValue());
    }
    public ResponseEntity<byte[]> downloadDocument(UUID documentUuid) {
        Document document = documentRepository.findByUuid(documentUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getFileData());
    }



    public Document saveDocument(MultipartFile file) throws IOException {
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileData(file.getBytes());
        document.setContentType(file.getContentType());
        document.setCreatedAt(LocalDateTime.now()); // Set the current date and time

        // Save the document entity to the database
        return documentRepository.save(document);
    }




    public Optional<Document> getDocumentById(Integer id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByDemandeId(Long demandeId) {
        return documentRepository.findByDemandeId(demandeId);
    }

    public List<Document> getDocumentsByReclamationId(Long reclamationId) {
        return documentRepository.findByReclamationId(reclamationId);
    }

    public List<Document> getDocumentByDemandeId(Long demandeId) {
        return documentRepository.findByDemandeId(demandeId);
    }





}
