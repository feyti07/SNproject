package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.ObjectsValidator;
import com.snim.demandesrh.entities.*;
import com.snim.demandesrh.entities.dto.ReclamationDto;
import com.snim.demandesrh.enums.ReclamationEnums.CategorieEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.repository.EmployeeRepository;
import com.snim.demandesrh.repository.HistoriqueRepository;
import com.snim.demandesrh.repository.ReclamationRepository;
import com.snim.demandesrh.repository.UserRepository;
import com.snim.demandesrh.service.IReclamationService;
import com.snim.demandesrh.service.auth.UserDetailsServiceImp;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Transactional
@RequiredArgsConstructor
@Service
public class ReclamationService implements IReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final HistoriqueRepository historiqueRepository;
    private final HistoriqueService historiqueService;
    private final EmployeeRepository employeeRepository;
    private final ObjectsValidator<ReclamationDto> validator;
    private final UserDetailsServiceImp userDetailsServiceImp;
    private final UserRepository userRepository;

    @Override
    public long save(ReclamationDto dto) {
        // Trouver l'employé par son matricule
        Optional<Employee> employeeOptional = employeeRepository.findByMatricule(dto.getEmployeeMatricule());
        Employee employee = employeeOptional.orElseThrow(() -> new IllegalArgumentException("Employee not found with matricule: " + dto.getEmployeeMatricule()));

        // Convertir le DTO en entité Reclamation
        Reclamation reclamation = ReclamationDto.toEntity(dto, employee);
        reclamation.setCreatedAt(LocalDateTime.now());
        reclamation.setCreateur(dto.getCreateur());

        // Gestion de l'upload de fichier
        if (dto.getPieceJointe() != null && dto.getPieceJointe().length > 0) {
            try {
                System.out.println("Processing file upload...");
                // Créer un nom de fichier unique
                String fileName = "piece_jointe_" + UUID.randomUUID() + ".dat";

                // Définir un chemin de stockage temporaire
                Path tempDir = Files.createTempDirectory("uploads"); // Crée un dossier temporaire
                Path filePath = tempDir.resolve(fileName); // Résoudre le nom de fichier

                // Écrire le fichier sur le disque
                Files.write(filePath, dto.getPieceJointe());

                byte[] fileData = Files.readAllBytes(filePath);
                System.out.println("fileData length: " + (fileData != null ? fileData.length : "null"));

                // Créer un objet Document pour représenter la pièce jointe
                Document document = new Document();
                document.setFileName(fileName);
                document.setFileLocation(filePath.toString()); // Stocker le chemin d'accès
                document.setReclamation(reclamation);
                document.setFileData(fileData);

                // Ajouter le document à la liste des pièces jointes de la réclamation
                reclamation.getPiecesJointes().add(document);

            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }

        // Définir le statut par défaut
        reclamation.setStatus(StatutEnum.NOUVEAU);

        // Enregistrer la réclamation
        Reclamation savedReclamation = reclamationRepository.save(reclamation);

        // Créer une entrée dans l'historique
        historiqueService.create(
                Historique.builder()
                        .acteur(dto.getCreateur())
                        .type("AJOUT")
                        .reclamation(savedReclamation)
                        .date(LocalDateTime.now())
                        .uuid(UUID.randomUUID())
                        .description("Création d'une nouvelle réclamation")
                        .build()
        );

        // Retourner l'identifiant de la réclamation enregistrée
        return savedReclamation.getId();
    }

    @Override
    public List<ReclamationDto> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // If user is admin, return all reclamations
            return reclamationRepository.findAll().stream()
                    .map(ReclamationDto::fromEntity)
                    .collect(Collectors.toList());
        } else {
            // If user is not admin, filter reclamations by their matricule
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            String matricule = userDetailsServiceImp.getMatriculeFromPrincipal(userDetails);
            return reclamationRepository.findAll().stream()
                    .filter(r -> r.getEmployee().getMatricule().equals(matricule))
                    .map(ReclamationDto::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ReclamationDto findById(Integer id) {
        return null;
    }

    @Override
    public void delete(Integer id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réclamation non trouvée"));
        reclamation.setIsArchived(true); // Marquer comme archivée
        reclamationRepository.save(reclamation); // Sauvegarder les modifications
    }

    @Override
    public void delete(Long id) {
        reclamationRepository.deleteById(id.intValue());
    }

    @Override
    public ReclamationDto findById(Long id) {
        return reclamationRepository.findById(id.intValue())
                .map(ReclamationDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("No reclamation was found with the ID :" + id));
    }


    @Override
    public ReclamationDto update(Integer id, ReclamationDto dto) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName(); // Cela récupère l'email de l'utilisateur

        // 2. Rechercher l'utilisateur par son email dans la base de données
        User user = userRepository.findByEmail(email) // Assurez-vous d'avoir cette méthode dans votre repository
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        // Récupérer le nom complet ou un autre champ de l'utilisateur
        String acteur = user.getName();  // Ou un autre champ selon vos besoins

        // 1. Vérifier si la réclamation existe
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Réclamation non trouvée avec l'ID: " + id));

        // 2. Récupérer l'employé par matricule uniquement s'il est fourni
        Employee employee = null;
        if (dto.getEmployeeMatricule() != null && !dto.getEmployeeMatricule().isEmpty()) {
            Optional<Employee> employeeOptional = employeeRepository.findByMatricule(dto.getEmployeeMatricule());
            employee = employeeOptional.orElse(null);
        }

        // 3. Mettre à jour les informations de la réclamation
        reclamation.setCategorie(dto.getCategorie());
        reclamation.setMatriculeEmploye(dto.getEmployeeMatricule());
        reclamation.setImpact(dto.getImpact());
        reclamation.setUrgence(dto.getUrgence());
        reclamation.setLieu(dto.getLieu());
        reclamation.setType(dto.getType());
        reclamation.setDescription(dto.getDescription());
        reclamation.setUpdatedAt(LocalDateTime.now());

        // Utiliser l'acteur récupéré
        reclamation.setCreateur(acteur); // Mettre à jour le créateur avec l'utilisateur connecté

        // NOTE: Ne pas toucher à la pièce jointe, elle ne sera pas modifiée

        // 4. Enregistrer les modifications (sans modification de la pièce jointe)
        Reclamation updatedReclamation = reclamationRepository.save(reclamation);

        // 5. Créer une entrée dans l'historique
        historiqueService.create(
                Historique.builder()
                        .acteur(acteur) // Utiliser l'acteur récupéré
                        .type("MODIFICATION")
                        .reclamation(updatedReclamation) // Utiliser l'objet réclamation mis à jour
                        .date(LocalDateTime.now())
                        .uuid(UUID.randomUUID())
                        .description("Mise à jour de la réclamation avec l'ID: " + id)
                        .build()
        );

        // 6. Retourner la réclamation mise à jour
        return ReclamationDto.fromEntity(updatedReclamation);
    }


    @Override
    public List<ReclamationDto> findAllForUser(UserDetails userDetails) {
        return null;
    }

    @Override
    public ReclamationDto update(Long reclamationId, ReclamationDto reclamationDto) {
        Reclamation existingReclamation = reclamationRepository.findById(reclamationId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("No reclamation was found with the ID: " + reclamationId));

        Optional<Employee> employeeOptional = employeeRepository.findByMatricule(reclamationDto.getEmployeeMatricule());
        Employee employee = employeeOptional.orElseThrow(() -> new IllegalArgumentException("Employee not found with matricule: " + reclamationDto.getEmployeeMatricule()));

        // Save the modification history
        historiqueService.create(
                Historique.builder()
                        .acteur(reclamationDto.getCreateur()) // Assurez-vous que l'acteur est fourni dans reclamationDto
                        .type("MODIFICATION")
                        .reclamation(existingReclamation)
                        .date(LocalDateTime.now())
                        .uuid(UUID.randomUUID())
                        .description("Modification de la réclamation ID : " + reclamationId)
                        .build()
        );

        // Update the reclamation
        existingReclamation.setLieu(reclamationDto.getLieu());
        existingReclamation.setCategorie(reclamationDto.getCategorie());
        existingReclamation.setUrgence(reclamationDto.getUrgence());
        existingReclamation.setImpact(reclamationDto.getImpact());
        existingReclamation.setEmployee(employee);
        existingReclamation.setDescription(reclamationDto.getDescription());
        existingReclamation.setPieceJointe(reclamationDto.getPieceJointe());
        existingReclamation.setType(reclamationDto.getType());
        existingReclamation.setStatus(reclamationDto.getStatus());
        existingReclamation.setUpdatedAt(LocalDateTime.now());

        reclamationRepository.save(existingReclamation);

        return ReclamationDto.fromEntity(existingReclamation);
    }

    @Override
    public List<ReclamationDto> getModifiedReclamations() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Reclamation> modifiedReclamations = reclamationRepository.findByUpdatedAtAfter(thirtyDaysAgo);

        return modifiedReclamations.stream()
                .map(ReclamationDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<Reclamation> getReclamations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reclamationRepository.findAll(pageable);
    }



    @Override
    public void updateStatut(Long reclamationId, StatutEnum statut) {
        Optional<Reclamation> optionalReclamation = reclamationRepository.findById(reclamationId.intValue());

        if (optionalReclamation.isPresent()) {
            Reclamation reclamation = optionalReclamation.get();
            reclamation.setStatus(statut);
            reclamationRepository.save(reclamation);
        } else {
            throw new RuntimeException("Reclamation not found with id: " + reclamationId);
        }
    }



    public List<Map<String, Object>> getTopReclamations() {
        Pageable pageable = PageRequest.of(0, 3); // Page 0, 3 items
        return reclamationRepository.findTopReclamations(pageable);
    }

    public Page<ReclamationDto> getPaginatedReclamations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reclamation> reclamationPage = reclamationRepository.findAll(pageable);

        // Convert Page<Reclamation> to Page<ReclamationDto>
        return reclamationPage.map(ReclamationDto::fromEntity);
    }

    public Map<String, Long> countReclamagtionByCategory() {
        return reclamationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        reclamation-> reclamation.getCategorie().toString(),  // Convert CategorieEnum to String
                        Collectors.counting()
                ));
    }
    public long getTotalReclamationsCount() {
        return reclamationRepository.count();
    }

    public long getTotalReclamations() {
        return reclamationRepository.count();
    }

    public Long countTotalReclamations() {
        return reclamationRepository.findAll().stream()
                .count();  // Count the total number of reclamations
    }



    public Long getEmployeeIdByReclamationId(Long reclamationId) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId.intValue())
                .orElseThrow(() -> new RuntimeException("Reclamation not found with ID: " + reclamationId));

        String matricule = reclamation.getEmployeeMatricule();
        if (matricule == null || matricule.isEmpty()) {
            throw new RuntimeException("Matricule is not specified in the reclamation");
        }

        Employee employee = employeeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));

        User user = employee.getUser();
        if (user == null) {
            throw new RuntimeException("User not found for employee with matricule: " + matricule);
        }

        return user.getId();
    }

    public String getEmployeeMatriculeByReclamationId(Long reclamationId) {
        Reclamation reclamation = reclamationRepository.findById(reclamationId.intValue())
                .orElseThrow(() -> new RuntimeException("Reclamation not found with ID: " + reclamationId));
        return reclamation.getEmployeeMatricule();
    }

    public Map<String, Long> countReclamationsByCategory() {
        return reclamationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        reclamation -> Optional.ofNullable(reclamation.getCategorie())
                                .map(CategorieEnum::toString)
                                .orElse("Autre"),  // Handle null categories by assigning a default value
                        Collectors.counting()
                ));
    }

    public List<ReclamationDto> findReclamationsByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String createur = userDetailsServiceImp.getMatriculeFromPrincipal(userDetails);

        return reclamationRepository.findByCreateur(createur)
                .stream()
                .map(ReclamationDto::fromEntity)
                .collect(Collectors.toList());
    }



}
