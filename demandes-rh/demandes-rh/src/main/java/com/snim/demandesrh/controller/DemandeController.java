package com.snim.demandesrh.controller;
import com.snim.demandesrh.entities.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snim.demandesrh.entities.dto.DemandeDto;
import com.snim.demandesrh.enums.DemandeEnums.CategorieEnum;
import com.snim.demandesrh.enums.DemandeEnums.LieuEnum;
import com.snim.demandesrh.enums.ImpactEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.enums.TypeEnum;
import com.snim.demandesrh.enums.UrgenceEnum;
import com.snim.demandesrh.exceptions.InvalidStatusException;
import com.snim.demandesrh.repository.DemandeRepository;
import com.snim.demandesrh.repository.ReclamationRepository;
import com.snim.demandesrh.service.IDemandeService;
import com.snim.demandesrh.service.impl.DemandeService;
import com.snim.demandesrh.service.impl.DocumentService;
import com.snim.demandesrh.service.impl.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/demandes")
public class DemandeController {
    @Autowired
    private IDemandeService iDemandeService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private DocumentService documentService;
    @Autowired
    private NotificationService notificationService;


    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> save(
            @RequestPart("demande") String demandeJson,
            @RequestParam(value = "documents", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = (User) userDetails;
        String createur = user.getName();

        try {
            // Convert JSON to DTO
            DemandeDto dto = objectMapper.readValue(demandeJson, DemandeDto.class);
            dto.setCreateur(createur);

            // 1. Save the demande
            long demandeId = demandeService.save(dto);

            // 2. Handle multiple documents
            if (files != null && !files.isEmpty()) {
                for (MultipartFile multipartFile : files) {
                    // Use the DocumentService to save the document
                    Document savedDocument = documentService.saveDocument(multipartFile);
                    documentService.addDocumentAsPieceJointe(demandeId, savedDocument);
                }
            }
            String message = "Nouvelle demande créée par " + createur;
            notificationService.createNotification(message, "RES");
            notificationService.createNotification(message, "ADMIN");

            // Return the ID in a JSON response
            return ResponseEntity.ok(Collections.singletonMap("demandeId", demandeId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du traitement: " + e.getMessage());
        }
    }


/*    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;

    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> save(
            @RequestPart("demande") String demandeJson,
            @RequestParam(value = "documents", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Récupérer les informations de l'utilisateur connecté
        User user = (User) userDetails;
        String createur = user.getName();

        try {
            // Convertir le JSON en DTO
            DemandeDto dto = objectMapper.readValue(demandeJson, DemandeDto.class);
            dto.setCreateur(createur);

            // 1. Enregistrer la demande sans pièces jointes
            long demandeId = demandeService.save(dto);

            // 2. Gérer les fichiers multiples
            if (files != null && !files.isEmpty()) {
                for (MultipartFile multipartFile : files) {
                    Document document = new Document();
                    document.setUuid(UUID.randomUUID());
                    document.setFileName(multipartFile.getOriginalFilename());

                    // Définir l'emplacement de stockage du fichier
                    String fileLocation = uploadDir + "/" + document.getFileName();

                    try {
                        // Transférer le fichier vers l'emplacement spécifié
                        multipartFile.transferTo(new File(fileLocation));
                        document.setFileLocation(fileLocation);
                        document.setFileData(multipartFile.getBytes()); // Stocker les données du fichier dans la base de données

                        // Appeler le service pour associer ce document à la demande
                        documentService.addDocumentAsPieceJointe(demandeId, document);

                    } catch (IOException e) {
                        // Logger l'erreur et lancer une exception
                        System.err.println("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
                        throw new RuntimeException("Erreur de transfert de fichier : " + e.getMessage(), e);
                    }
                }
            }

            return ResponseEntity.ok("Demande enregistrée avec ID : " + demandeId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du traitement : " + e.getMessage());
        }
    }*/








    @PostMapping("/create")
    public ResponseEntity<DemandeDto> createDemande(@RequestBody DemandeDto demandeDto) {
        // Appel au service pour créer la demande
        DemandeDto createdDemande = demandeService.createDemande(demandeDto);
        // Retourne la demande créée avec un statut HTTP 201 Created
        return new ResponseEntity<>(createdDemande, HttpStatus.CREATED);
    }





    @PostMapping("/{demandeId}/piece-jointe")
    public ResponseEntity<DemandeDto> addPieceJointe(@PathVariable Long demandeId,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            // Appel au service pour ajouter la pièce jointe
            DemandeDto updatedDemande = demandeService.addPieceJointe(demandeId, file);
            return new ResponseEntity<>(updatedDemande, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<List<DemandeDto>> findAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Vérifier si l'utilisateur a le rôle "ROLE_ADMIN" ou "ROLE_RES"
        boolean isAdminOrRes = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_RES"));

        if (isAdminOrRes) {
            // Si l'utilisateur est admin ou responsable, renvoyer toutes les demandes non archivées
            return ResponseEntity.ok(iDemandeService.findAll());
        } else {
            // Sinon, renvoyer seulement les demandes associées à l'utilisateur
            return ResponseEntity.ok(iDemandeService.findAllForUser(userDetails)); // Nouvelle méthode à créer dans le service
        }
    }


    @GetMapping("/{demande-id}")
    public ResponseEntity<DemandeDto> findById(
            @PathVariable("demande-id") Integer demandeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DemandeDto demandeDto = iDemandeService.findById(demandeId);

        return ResponseEntity.ok(demandeDto);
    }

    // Autres méthodes de contrôleur comme delete, update, etc.

    // Méthode utilitaire pour récupérer le matricule à partir de UserDetails
    private String getMatriculeFromPrincipal(UserDetails userDetails) {
        // Ceci est une implémentation simplifiée, vous devez adapter cette méthode à votre logique d'authentification
        // Supposons que le matricule soit stocké dans le username ou d'autres détails personnalisés
        return userDetails.getUsername(); // Ou userDetails.getMatricule() selon votre implémentation de UserDetails
    }




   /* @PostMapping(path = "/create")
    public ResponseEntity<?> save(
            @RequestBody DemandeDto demandeDto
    ) {
        return ResponseEntity.ok(iDemandeService.save(demandeDto));
    }

    @GetMapping("/list")
    public ResponseEntity<List<DemandeDto>> findAll() {
        return ResponseEntity.ok(iDemandeService.findAll());
    }

    @GetMapping("/{demande-id}")
    public ResponseEntity<DemandeDto> findById(
            @PathVariable("demande-id") Integer demandeId
    ) {
        return ResponseEntity.ok(iDemandeService.findById(demandeId));
    }*/

    @DeleteMapping("/d/{demande-id}")
    public ResponseEntity<Void> delete(@PathVariable("demande-id") Integer demandeId) {
        iDemandeService.delete(demandeId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/archives")
    public List<Demande> getArchivedDemandes() {
        return demandeService.getArchivedDemandes();
    }





    /*@PutMapping("/{demande-id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("demande-id") Integer demandeId,
            @RequestBody DemandeDto demandeDto
    ) {
        try {
            // Ajoutez des journaux pour voir les valeurs reçues
            System.out.println("Demande ID: " + demandeId);
            System.out.println("Nouveau statut: " + demandeDto.getStatus());

            // Convertir StatutEnum en String
            String status = demandeDto.getStatus().toString();
            iDemandeService.updateStatus(demandeId, status);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            // Log spécifique pour l'entité non trouvée
            System.err.println("Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Demande non trouvée avec l'ID: " + demandeId);
        } catch (InvalidStatusException e) {
            // Log spécifique pour le statut invalide
            System.err.println("Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Statut invalide: " + demandeDto.getStatus());
        } catch (Exception e) {
            // Log pour toute autre exception
            System.err.println("Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la mise à jour du statut de la demande.");
        }

    }
*/
   /* @PostMapping(path = "/createWithFile", consumes = "multipart/form-data")
    public ResponseEntity<?> saveWithFormData(
            @RequestParam("lieu") String lieu,
            @RequestParam("categorie") String categorie,
            @RequestParam("urgence") String urgence,
            @RequestParam("impact") String impact,
            @RequestParam("employeeMatricule") String employeeMatricule,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("pieceJointe") MultipartFile pieceJointe
    ) {
        DemandeDto demandeDto = new DemandeDto();
        demandeDto.setLieu(LieuEnum.valueOf(lieu));
        demandeDto.setCategorie(CategorieEnum.valueOf(categorie));
        demandeDto.setUrgence(UrgenceEnum.valueOf(urgence));
        demandeDto.setImpact(ImpactEnum.valueOf(impact));
        demandeDto.setEmployeeMatricule(employeeMatricule);
        demandeDto.setDescription(description);
        demandeDto.setType(TypeEnum.valueOf(type));

        if (pieceJointe != null && !pieceJointe.isEmpty()) {
            demandeDto.setPieceJointe(pieceJointe.getOriginalFilename());
            // Sauvegardez le fichier joint selon votre logique
        }

        long savedDemande = iDemandeService.save(demandeDto);
        return ResponseEntity.ok(savedDemande);
    }*/


    @PutMapping("/u/{demande-id}")
    public ResponseEntity<?> update(
            @PathVariable("demande-id") Integer demandeId,
            @RequestBody DemandeDto demandeDto
    ) {
        try {
            DemandeDto updatedDemande = iDemandeService.update(demandeId, demandeDto);
            return ResponseEntity.ok(updatedDemande);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la mise à jour de la demande.");
        }
    }

    @GetMapping("/modified")
    public List<DemandeDto> getModifiedDemandes() {
        return iDemandeService.getModifiedDemandes();
    }

    /*@GetMapping("/{demande-id}/desc")
    public ResponseEntity<List<String>> getModificationsDescriptionsForDemande(@PathVariable("demande-id") Long demandeId) {
        List<String> modificationsDescriptions = iDemandeService.getModificationsDescriptionsForDemande(demandeId);
        return ResponseEntity.ok(modificationsDescriptions);
    }*/

    @GetMapping("/demandes")
    public Page<Demande> getDemandes(
            @RequestParam int page,
            @RequestParam int size) {
        return demandeService.getDemands(page, size);
    }

    @GetMapping("/demandes/count")
    public long getTotalDemandes() {
        return demandeService.getTotalDemandes();
    }

    @PutMapping("/{demandeId}/update-status")
    public ResponseEntity<?> updateStatut(@PathVariable Integer demandeId, @RequestBody Map<String, String> payload) {
        try {
            String statutStr = payload.get("status");
            String updatedBy = payload.get("updatedBy"); // Fetch updatedBy from payload
            StatutEnum statut = StatutEnum.valueOf(statutStr);

            // Pass updatedBy to the service method
            demandeService.updateStatut(demandeId, statut, updatedBy);

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/count")
    public long getTotalDemandesCount() {
        return demandeService.getTotalDemandesCount();
    }

    @GetMapping("/dem-top")
    public ResponseEntity<List<Map<String, Object>>> getTopDemandes() {
        List<Map<String, Object>> topDemandes = demandeService.getTopDemandes();
        return ResponseEntity.ok(topDemandes);
    }

    @GetMapping("/{demandeId}/employee-id")
    public Long getEmployeeIdByDemandeId(@PathVariable Long demandeId) {
        return demandeService.getEmployeeIdByDemandeId(demandeId);
    }

    @GetMapping("/{demandeId}/matricule")
    public String getEmployeeMatricule(@PathVariable Long demandeId) {
        return demandeService.getEmployeeMatriculeByDemandeId(demandeId);
    }

    @GetMapping("/categories/count")
    public ResponseEntity<Map<String, Long>> getDemandCountByCategory() {
        Map<String, Long> demandCountByCategory = demandeService.countDemandsByCategory();
        return ResponseEntity.ok(demandCountByCategory);
    }

    @GetMapping("/userMatricule/{demandeId}")
    public ResponseEntity<Long> getUserIdByDemandeId(@PathVariable Long demandeId) {
        try {
            Long userId = demandeService.findUserIdByDemandeId(demandeId);
            return ResponseEntity.ok(userId);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found if demande or user is not found
        }
    }

    @GetMapping("/user-list")
    public List<DemandeDto> getDemandsByCurrentUser() {
        return demandeService.findDemandsByCurrentUser();
    }




    @GetMapping("/count-demandeurs")
    public ResponseEntity<Long> countTotalDemandeursWithUserRole() {
        long count = demandeService.countTotalDemandeursWithUserRole();
        return ResponseEntity.ok(count);
    }




    @GetMapping("/countMat")
    public ResponseEntity<Map<String, Integer>> countUniqueMatricules(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Vérifier si l'utilisateur a le rôle "ROLE_ADMIN" ou "ROLE_RES"
        boolean isAdminOrRes = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_RES"));

        List<DemandeDto> demandes;
        if (isAdminOrRes) {
            // Si l'utilisateur est admin ou responsable, renvoyer toutes les demandes non archivées
            demandes = iDemandeService.findAll();
        } else {
            // Sinon, renvoyer seulement les demandes associées à l'utilisateur
            demandes = iDemandeService.findAllForUser(userDetails);
        }

        // Compter les matricules uniques
        int uniqueMatriculeCount = demandeService.countUniqueMatricules(demandes);

        // Créer la réponse avec le compte de matricules uniques
        Map<String, Integer> response = new HashMap<>();
        response.put("uniqueMatriculeCount", uniqueMatriculeCount);

        return ResponseEntity.ok(response);
    }



}






