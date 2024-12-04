package com.snim.demandesrh.controller;
import com.snim.demandesrh.entities.Document;
import com.snim.demandesrh.entities.dto.DocumentDto;
import com.snim.demandesrh.service.impl.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/{demandeId}", consumes = "multipart/form-data")
    public ResponseEntity<Document> addDocumentAsPieceJointe(
            @PathVariable Long demandeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uuid", required = false) UUID uuid,
            @RequestParam(value = "fileName", required = false) String fileName) {
        logger.info("Received request to add document: ");
        logger.info("Demande ID: " + demandeId);
        logger.info("File: " + (file != null ? file.getOriginalFilename() : "No file"));
        logger.info("UUID: " + uuid);
        logger.info("File Name: " + fileName);

        // Création d'une nouvelle instance de Document
        Document document = new Document();

        // Générer un nouvel UUID si aucun n'est fourni
        if (uuid != null) {
            document.setUuid(uuid);
        } else {
            document.setUuid(UUID.randomUUID());
        }

        // Utiliser le nom original du fichier si aucun nom n'est fourni
        if (fileName != null) {
            document.setFileName(fileName);
        } else {
            document.setFileName(file.getOriginalFilename());
        }

        // Définir l'emplacement du fichier (fileLocation)
        String fileLocation = "C:/Users/HP/Desktop/" + document.getFileName();
        document.setFileLocation(fileLocation);

        // Sauvegarder le fichier sur le système de fichiers
        try {
            file.transferTo(new File(fileLocation));  // Stockage du fichier
        } catch (IOException e) {
            // Gérer l'exception, retourner une erreur appropriée
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Appeler le service pour enregistrer le document associé à la demande
        Document savedDocument = documentService.addDocumentAsPieceJointe(demandeId, document);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
    }
/*
    @GetMapping(value = "/{demandeId}")
    public ResponseEntity<?> getDocumentsByDemandeId(@PathVariable Long demandeId) {
        // Récupérer la liste des documents associés à la demande
        List<Document> documents = documentService.getDocumentsByDemandeId(demandeId);

        if (documents.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Aucun document trouvé pour la demande ID : " + demandeId);
        }

        // Parcourir les documents et déterminer leur type
        List<ResponseEntity<byte[]>> documentResponses = new ArrayList<>();

        for (Document document : documents) {
            File file = new File(document.getFileLocation());

            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Le fichier " + document.getFileName() + " est introuvable.");
            }

            // Déterminer le type MIME du fichier
            String mimeType;
            try {
                mimeType = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la détermination du type de fichier.");
            }

            // Lire le contenu du fichier
            byte[] fileContent;
            try {
                fileContent = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la lecture du fichier.");
            }

            // Créer la réponse en fonction du type MIME
            if (mimeType != null && mimeType.startsWith("image/")) {
                // Si c'est une image, renvoyer le contenu en tant qu'image
                documentResponses.add(ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(fileContent));
            } else {
                // Si c'est un document (PDF, Word, etc.), renvoyer le fichier avec le bon type MIME
                documentResponses.add(ResponseEntity.ok()
                        .header("Content-Disposition", "inline; filename=\"" + document.getFileName() + "\"")
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(fileContent));
            }
        }

        // Retourner la liste des documents
        return ResponseEntity.ok(documentResponses);
    }*/

  /*  @GetMapping("/{demandeId}")
    public List<DocumentDto> getDocumentsByDemandeId1(@PathVariable Long demandeId) {
        List<Document> documents = documentService.getDocumentsByDemandeId(demandeId);
        return documents.stream()
                .map(DocumentDto::fromEntity) // Utiliser la méthode fromEntity pour convertir
                .collect(Collectors.toList());
    }*/


    @GetMapping("/download/{uuid}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID uuid) {
        return documentService.downloadDocument(uuid);
    }

    @PostMapping
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Uploading file: " + file.getOriginalFilename());
            System.out.println("Content Type: " + file.getContentType()); // Log content type

            Document savedDocument = documentService.saveDocument(file);
            return ResponseEntity.ok(savedDocument);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null); // Handle file save errors
        }
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer id) {
        Optional<Document> documentOptional = documentService.getDocumentById(id);

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            byte[] fileData = document.getFileData();

            // Log the content type and size of the retrieved file data
            System.out.println("Retrieved document content type: " + document.getContentType());
            System.out.println("Retrieved document file size: " + fileData.length + " bytes");

            // Create the output directory if it doesn't exist
            String outputDirPath = "output"; // Change this to your preferred path
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                boolean dirCreated = outputDir.mkdir(); // Create the directory
                if (dirCreated) {
                    System.out.println("Output directory created at: " + outputDirPath);
                } else {
                    System.err.println("Failed to create output directory.");
                }
            }

            // Write the file data to a temporary file for debugging
            try {
                String filePath = outputDirPath + "/" + document.getFileName(); // Specify the path where the file will be created
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(fileData);
                fos.close();
                System.out.println("Temporary file created at: " + filePath);
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
            }

            HttpHeaders headers = new HttpHeaders();
            String contentType = document.getContentType();

            // Set headers appropriately
            if (contentType == null) {
                contentType = "application/octet-stream";  // Default for unknown types
            }

            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            // Handle Content-Disposition for Word documents
            if (contentType.startsWith("application/vnd.openxmlformats-officedocument")) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\"");
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + document.getFileName() + "\"");
            }

            // Return the file data with appropriate headers
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getByDemandeId/{demandeId}")
    public ResponseEntity<byte[]> getDocumentByDemandeId(@PathVariable Long demandeId) {
        // Fetch the list of documents based on demandeId
        List<Document> documents = documentService.getDocumentsByDemandeId(demandeId);

        if (!documents.isEmpty()) {
            Document document = documents.get(0);  // Get the first document
            byte[] fileData = document.getFileData();

            HttpHeaders headers = new HttpHeaders();
            String contentType = document.getContentType() != null ? document.getContentType() : "application/octet-stream";
            headers.setContentType(MediaType.parseMediaType(contentType));  // Set content type based on document

            // If you want to display the image inline, you can skip the attachment disposition.
            // headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"");

            // Return the file data with appropriate headers
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getByReclamationId/{reclamationId}")
    public ResponseEntity<byte[]> getDocumentByReclamationId(@PathVariable Long reclamationId) {
        // Fetch the list of documents based on reclamationId
        List<Document> documents = documentService.getDocumentsByReclamationId(reclamationId);

        if (!documents.isEmpty()) {
            Document document = documents.get(0);  // Get the first document
            byte[] fileData = document.getFileData();

            HttpHeaders headers = new HttpHeaders();
            String contentType = document.getContentType() != null ? document.getContentType() : "application/octet-stream";
            headers.setContentType(MediaType.parseMediaType(contentType));  // Set content type based on document

            // If you want to display the image inline, you can skip the attachment disposition.
            // headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"");

            // Return the file data with appropriate headers
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
