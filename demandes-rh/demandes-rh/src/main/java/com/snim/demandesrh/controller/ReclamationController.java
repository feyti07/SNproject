package com.snim.demandesrh.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.snim.demandesrh.entities.Document;
import com.snim.demandesrh.enums.ReclamationEnums.CategorieEnum;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.Reclamation;
import com.snim.demandesrh.entities.dto.ReclamationDto;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.exceptions.InvalidStatusException;
import com.snim.demandesrh.repository.EmployeeRepository;
import com.snim.demandesrh.repository.ReclamationRepository;
import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.IReclamationService;
import com.snim.demandesrh.service.auth.UserDetailsServiceImp;
import com.snim.demandesrh.service.impl.DocumentService;
import com.snim.demandesrh.service.impl.ReclamationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.snim.demandesrh.entities.User;

@RestController
@RequestMapping("/api/reclamations")
public class ReclamationController {

    @Autowired
    private IReclamationService iReclamationService;
    @Autowired
    private ReclamationService reclamationService;

    @Autowired
    private ReclamationRepository reclamationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserDetailsServiceImp userDetailsServiceImp;
    @Autowired
    private DocumentService documentService;



    @PostMapping("/create")
    public ResponseEntity<?> save(
            @RequestBody ReclamationDto reclamationDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Cast de UserDetails en User
        User user = (User) userDetails;

        // Définir le nom de l'utilisateur connecté
        reclamationDto.setCreateur(user.getName());

        // Vérifier le rôle de l'utilisateur
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // Si l'utilisateur n'est pas admin, s'assurer que la réclamation est créée avec son propre matricule
            reclamationDto.setEmployeeMatricule(getMatriculeFromPrincipal(user));
        }

        return ResponseEntity.ok(iReclamationService.save(reclamationDto));
    }

    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> saveReclamation(
            @RequestPart("reclamation") String reclamationJson,
            @RequestParam(value = "documents", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = (User) userDetails;
        String createur = user.getName();

        try {
            // Convert JSON to DTO
            ReclamationDto dto = objectMapper.readValue(reclamationJson, ReclamationDto.class);
            dto.setCreateur(createur);

            // 1. Save the reclamation
            long reclamationId = reclamationService.save(dto);

            // 2. Handle multiple documents
            if (files != null && !files.isEmpty()) {
                for (MultipartFile multipartFile : files) {
                    // Use the DocumentService to save the document
                    Document savedDocument = documentService.saveDocument(multipartFile);
                    documentService.addDocumentAsPJ(reclamationId, savedDocument);
                }
            }

            // Return the ID in a JSON response
            return ResponseEntity.ok(Collections.singletonMap("reclamationId", reclamationId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du traitement: " + e.getMessage());
        }
    }

    @DeleteMapping("/del/{reclamation-id}")
    public ResponseEntity<Void> delete(@PathVariable("reclamation-id") Integer reclamationId) {
        iReclamationService.delete(reclamationId);
        return ResponseEntity.accepted().build();
    }



    @GetMapping("/list")
    public ResponseEntity<List<ReclamationDto>> findAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Vérifier le rôle de l'utilisateur
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // Si l'utilisateur n'est pas admin, filtrer pour récupérer seulement ses propres réclamations
            return ResponseEntity.ok(iReclamationService.findAll());
        }
        // Sinon, renvoyer toutes les réclamations pour les admins
        return ResponseEntity.ok(iReclamationService.findAll());
    }

    @GetMapping("/{reclamation-id}")
    public ResponseEntity<ReclamationDto> findById(
            @PathVariable("reclamation-id") Integer reclamationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReclamationDto reclamationDto = iReclamationService.findById(reclamationId);
        // Vérifier le rôle de l'utilisateur
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // Si l'utilisateur n'est pas admin, vérifier que la réclamation appartient à l'utilisateur
            if (!reclamationDto.getEmployeeMatricule().equals(getMatriculeFromPrincipal(userDetails))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(reclamationDto);
    }



    @PutMapping("/up/{reclamationId}") // Changez ici
    public ResponseEntity<?> update(
            @PathVariable("reclamationId") Integer reclamationId, // Assurez-vous que le nom ici correspond
            @RequestBody ReclamationDto reclamationDto
    ) {
        try {
            System.out.println("Reclamation ID: " + reclamationId);
            System.out.println("Reclamation DTO: " + reclamationDto);

            ReclamationDto updatedReclamation = iReclamationService.update(reclamationId, reclamationDto);
            return ResponseEntity.ok(updatedReclamation);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Ajoutez cette ligne pour voir la stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la mise à jour de la réclamation.");
        }
    }


    @PostMapping(path = "/createWithFile", consumes = "multipart/form-data")
    public ResponseEntity<?> saveWithFormData(
            @RequestParam("employeeMatricule") String employeeMatricule,
            @RequestParam("description") String description,
            @RequestParam("status") String status,
            @RequestParam("pieceJointe") MultipartFile pieceJointe
    ) {
        ReclamationDto reclamationDto = new ReclamationDto();
        reclamationDto.setEmployeeMatricule(employeeMatricule);
        reclamationDto.setDescription(description);
        reclamationDto.setStatus(StatutEnum.valueOf(status));

        if (pieceJointe != null && !pieceJointe.isEmpty()) {
            reclamationDto.setPieceJointe(pieceJointe.getOriginalFilename().getBytes());
            // Sauvegardez le fichier joint selon votre logique
        }

        long savedReclamation = iReclamationService.save(reclamationDto);
        return ResponseEntity.ok(savedReclamation);
    }

    @PutMapping("/u/{reclamation-id}")
    public ResponseEntity<?> update(
            @PathVariable("reclamation-id") Long reclamationId,
            @RequestBody ReclamationDto reclamationDto
    ) {
        try {
            ReclamationDto updatedReclamation = iReclamationService.update(reclamationId, reclamationDto);
            return ResponseEntity.ok(updatedReclamation);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la mise à jour de la réclamation.");
        }
    }

    @GetMapping("/modified")
    public List<ReclamationDto> getModifiedReclamations() {
        return iReclamationService.getModifiedReclamations();
    }

    @PutMapping("/{reclamationId}/update-status")
    public ResponseEntity<?> updateStatut(@PathVariable Long reclamationId, @RequestBody Map<String, String> payload) {
        try {
            String statutStr = payload.get("status");
            StatutEnum statut = StatutEnum.valueOf(statutStr);
            iReclamationService.updateStatut(reclamationId, statut);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getMatriculeFromPrincipal(UserDetails userDetails) {
        // Ceci est une implémentation simplifiée, vous devez adapter cette méthode à votre logique d'authentification
        // Supposons que le matricule soit stocké dans le username ou d'autres détails personnalisés
        return userDetails.getUsername(); // Ou userDetails.getMatricule() selon votre implémentation de UserDetails
    }

  /*  @GetMapping("/rec-top")
    public ResponseEntity<List<Map<String, Object>>> getTopReclamations() {
        List<Map<String, Object>> topReclamations = reclamationService.getTopReclamations();
        return ResponseEntity.ok(topReclamations);
    }*/

    @GetMapping("/paginated")
    public ResponseEntity<Page<ReclamationDto>> getPaginatedReclamations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReclamationDto> reclamations = reclamationService.getPaginatedReclamations(page, size);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/reclamations/count")
    public ResponseEntity<Long> countTotalReclamations() {
        Long totalReclamations = reclamationService.countTotalReclamations();
        return ResponseEntity.ok(totalReclamations);
    }
    @GetMapping("/categories/count")
    public ResponseEntity<Map<String, Long>> getReclamationCountByCategory() {
        Map<String, Long> reclamationCountByCategory = reclamationService.countReclamationsByCategory();
        return ResponseEntity.ok(reclamationCountByCategory);
    }




}








