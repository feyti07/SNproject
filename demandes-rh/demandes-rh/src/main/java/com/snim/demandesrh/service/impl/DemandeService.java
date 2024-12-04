package com.snim.demandesrh.service.impl;

import com.snim.demandesrh.ObjectsValidator;
import com.snim.demandesrh.entities.*;
import com.snim.demandesrh.entities.dto.DemandeDto;
import com.snim.demandesrh.entities.dto.ReclamationDto;
import com.snim.demandesrh.enums.DemandeEnums.LieuEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.repository.*;
import com.snim.demandesrh.service.IDemandeService;
import com.snim.demandesrh.service.IReclamationService;
import com.snim.demandesrh.service.auth.UserDetailsServiceImp;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.snim.demandesrh.enums.DemandeEnums.CategorieEnum;
@Transactional
@RequiredArgsConstructor
@Service
public class DemandeService implements IDemandeService {


    private final DemandeRepository demandeRepository;
    private final NotificationService notificationService;
    private final HistoriqueRepository historiqueRepository;
    private final HistoriqueService historiqueService;
    private final EmployeeRepository employeeRepository;
    private final ObjectsValidator<DemandeDto> validator;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    private final ReclamationRepository reclamationRepository;
    public long countTotalDemandeursWithUserRole() {
        // Récupérer les noms des créateurs qui ont soumis des demandes
        Set<String> demandeursDemandes = demandeRepository.findAll()
                .stream()
                .map(Demande::getCreateur) // Récupérer le nom du créateur de la demande
                .filter(Objects::nonNull) // Filtrer les créateurs non null
                .collect(Collectors.toSet());

        // Récupérer les noms des créateurs qui ont soumis des réclamations
        Set<String> demandeursReclamations = reclamationRepository.findAll()
                .stream()
                .map(Reclamation::getCreateur) // Récupérer le nom du créateur de la réclamation
                .filter(Objects::nonNull) // Filtrer les créateurs non null
                .collect(Collectors.toSet());

        // Fusionner les deux sets pour obtenir des créateurs uniques
        demandeursDemandes.addAll(demandeursReclamations);

        // Compter le nombre de créateurs ayant le rôle "USER"
        return demandeursDemandes.stream()
                .map(createur -> userRepository.findByUsername(createur)) // Rechercher l'utilisateur par nom de créateur
                .filter(Optional::isPresent) // Vérifier que l'utilisateur existe
                .map(Optional::get) // Extraire l'utilisateur de l'Optional
                .filter(user -> user.getRole() != null) // Vérifier que l'utilisateur a un rôle
                .filter(user -> "USER".equals(user.getRole().getName())) // Vérifier que l'utilisateur a le rôle "USER"
                .count();
    }
private final UserService userService;


    @Override
    public long save(DemandeDto dto) {
        Optional<Employee> employeeOptional = employeeRepository.findByMatricule(dto.getEmployeeMatricule());
        Employee employee = employeeOptional.orElseThrow(() -> new IllegalArgumentException("Employee not found with matricule: " + dto.getEmployeeMatricule()));
        Demande demande = DemandeDto.toEntity(dto, employee);
        demande.setCreatedAt(LocalDateTime.now());
        demande.setCreateur(dto.getCreateur());

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
                document.setDemande(demande);
                document.setFileData(fileData);

                // Ajouter le document à la liste des pièces jointes de la demande
                demande.getPiecesJointes().add(document);

            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }

        // Définir le statut par défaut
        demande.setStatus(StatutEnum.NOUVEAU);

        // Enregistrer la demande
        Demande savedDemande = demandeRepository.save(demande);

        // Créer une entrée dans l'historique
        historiqueService.create(
                Historique.builder()
                        .acteur(dto.getCreateur())
                        .type("AJOUT")
                        .demande(savedDemande)
                        .date(LocalDateTime.now())
                        .uuid(UUID.randomUUID())
                        .description("Création d'une nouvelle demande")
                        .build()
        );

        // Retourner l'identifiant de la demande enregistrée
        return savedDemande.getId();
    }

    @Override
    public DemandeDto update(Integer id, DemandeDto dto) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName(); // Cela récupère l'email de l'utilisateur

        // 2. Rechercher l'utilisateur par son email dans la base de données
        User user = userRepository.findByEmail(email) // Assurez-vous d'avoir cette méthode dans votre repository
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        // Récupérer le nom complet ou un autre champ de l'utilisateur
        String acteur = user.getName();  // Ou un autre champ selon vos besoins

        // 1. Vérifier si la demande existe
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Demande non trouvée avec l'ID: " + id));

        // 2. Récupérer l'employé par matricule uniquement s'il est fourni
        Employee employee = null;
        if (dto.getEmployeeMatricule() != null && !dto.getEmployeeMatricule().isEmpty()) {
            Optional<Employee> employeeOptional = employeeRepository.findByMatricule(dto.getEmployeeMatricule());
            employee = employeeOptional.orElse(null);
        }

        // 3. Mettre à jour les informations de la demande
        demande.setCategorie(dto.getCategorie());
        demande.setMatriculeEmploye(dto.getEmployeeMatricule());
        demande.setImpact(dto.getImpact());
        demande.setUrgence(dto.getUrgence());
        demande.setLieu(dto.getLieu());
        demande.setType(dto.getType());
        demande.setDescription(dto.getDescription());
        demande.setUpdatedAt(LocalDateTime.now());

        // Utiliser l'acteur récupéré
        demande.setCreateur(acteur); // Mettre à jour le créateur avec l'utilisateur connecté

        // NOTE: Ne pas toucher à la pièce jointe, elle ne sera pas modifiée

        // 4. Enregistrer les modifications (sans modification de la pièce jointe)
        Demande updatedDemande = demandeRepository.save(demande);

        // 5. Créer une entrée dans l'historique
        historiqueService.create(
                Historique.builder()
                        .acteur(acteur) // Utiliser l'acteur récupéré
                        .type("MODIFICATION")
                        .demande(updatedDemande)
                        .date(LocalDateTime.now())
                        .uuid(UUID.randomUUID())
                        .description("Mise à jour de la demande avec l'ID: " + id)
                        .build()
        );

        // 6. Retourner la demande mise à jour
        return DemandeDto.fromEntity(updatedDemande);
    }



    public DemandeDto createDemande(DemandeDto demandeDto) {
        // Récupérer l'employé en fonction du matricule dans le DTO
        Employee employee = employeeRepository.findByMatricule(demandeDto.getEmployeeMatricule())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Créer la nouvelle demande à partir du DTO
        Demande demande = DemandeDto.toEntity(demandeDto, employee);
        LieuEnum lieuEnum = demandeDto.getLieu();
        demande.setCreateur(demandeDto.getCreateur());
        demande.setCreatedAt(LocalDateTime.now());
        demande.setStatus(StatutEnum.NOUVEAU); // Assuming `getLieu()` returns a `LieuEnum`

        if (lieuEnum == null) {
            // Laisser le champ lieu vide ou définir une valeur par défaut, par exemple : demande.setLieu(null);
        } else {
            // Appliquer la valeur de l'enum seulement si 'lieu' n'est pas null
            demande.setLieu(lieuEnum);
        }

        // Défaut à NOUVEAU

        // Enregistrer la demande dans la base de données
        Demande savedDemande = demandeRepository.save(demande);

        return DemandeDto.fromEntity(savedDemande);
    }

    public Document addDocument(Document document) {
        // Logic for saving the document
        return documentRepository.save(document);
    }

    public List<Document> getDocumentsByDemandeId(Long demandeId) {
        // Logic for retrieving documents by demande ID
        return documentRepository.findByDemandeId(demandeId);
    }

    public DemandeDto addPieceJointe(Long demandeId, MultipartFile file) throws IOException {
        // Récupérer la demande existante
        Demande demande = demandeRepository.findById(demandeId.intValue())
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        // Ajouter la pièce jointe à la demande
        demande.setPieceJointe(file.getBytes());

        // Enregistrer la demande mise à jour
        Demande updatedDemande = demandeRepository.save(demande);

        // Retourner le DTO de la demande mise à jour
        return DemandeDto.fromEntity(updatedDemande);
    }
    @Override
    public long saveDemande(DemandeDto demandeDto, MultipartFile pieceJointe, String customFilePath, UserDetails userDetails) throws IOException {
        // Récupérer l'utilisateur connecté
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Créer une nouvelle demande à partir du DTO
        Demande demande = new Demande();
        demande.setDescription(demandeDto.getDescription());
        demande.setStatut(StatutEnum.NOUVEAU); // Ajouter le statut par défaut "NOUVEAU"
        demande.setCreatedAt(LocalDateTime.now());
        demande.setCreateur(currentUser.getUsername());

        // Si un fichier joint est présent, créer un document et l'associer à la demande
        if (pieceJointe != null && !pieceJointe.isEmpty()) {
            Document document = new Document();
            document.setFileName(pieceJointe.getOriginalFilename());
            document.setFileLocation(saveFileToDisk(pieceJointe, customFilePath)); // Utiliser le chemin personnalisé
            document.setUuid(UUID.randomUUID());
            document.setDemande(demande); // Associer le document à la demande
            documentRepository.save(document); // Enregistrer le document
        }

        // Enregistrer la demande dans la base de données
        return demandeRepository.save(demande).getId();
    }


    // Méthode pour enregistrer le fichier sur le disque
    private String saveFileToDisk(MultipartFile file, String destinationPath) throws IOException {
        Path path = Paths.get(destinationPath + file.getOriginalFilename());
        Files.write(path, file.getBytes());
        return path.toString(); // Retourner le chemin où le fichier est sauvegardé
    }

    @Override
    public Optional<Demande> findById(long id) {
        // Convert long to Integer
        if (id > Integer.MAX_VALUE || id < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Long value out of Integer range");
        }
        Integer integerId = (int) id;
        return demandeRepository.findById(integerId);
    }

    private final UserDetailsServiceImp userDetailsServiceImp;

    @Override
    public List<DemandeDto> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifier si l'utilisateur est un admin ou a le rôle "ROLE_RES"
        boolean isAdminOrRes = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_RES"));

        if (isAdminOrRes) {
            // Si l'utilisateur est admin ou a le rôle "ROLE_RES", retourner toutes les demandes non archivées
            return demandeRepository.findByIsArchivedFalse()
                    .stream()
                    .sorted(Comparator.comparing(Demande::getCreatedAt).reversed()) // Tri décroissant par date de création
                    .map(DemandeDto::fromEntity)
                    .collect(Collectors.toList());
        }

        // Vérifier si l'utilisateur a le rôle "ROLE_USER"
        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_USER"));

        if (isUser) {
            // Utiliser userService pour obtenir le matricule
            String matricule = userService.getMatriculeFromAuthentication(authentication);

            // Retourner uniquement les demandes liées au matricule de l'utilisateur
            return demandeRepository.findByIsArchivedFalseAndEmployee_Matricule(matricule)
                    .stream()
                    .sorted(Comparator.comparing(Demande::getCreatedAt).reversed()) // Tri décroissant par date de création
                    .map(DemandeDto::fromEntity)
                    .collect(Collectors.toList());
        }

        // Si l'utilisateur n'a aucun des rôles spécifiés
        throw new AccessDeniedException("Accès refusé : vous n'avez pas les permissions nécessaires.");
    }

    @Override
    public void delete(Integer id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Demande non trouvée"));
        demande.setIsArchived(true); // Marquer comme archivée
        demandeRepository.save(demande); // Sauvegarder les modifications
    }

    public List<Demande> getArchivedDemandes() {
        return demandeRepository.findByIsArchivedTrue(); // Assurez-vous d'avoir cette méthode dans le repository
    }


    @Override
    public DemandeDto findById(Integer id) {
        // Call findById method of repository, which returns Optional<Demande>
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No demande found with the ID: " + id));

        // Convert the found Demande entity to DemandeDto and return
        return DemandeDto.fromEntity(demande);
    }

    public List<DemandeDto> getModifiedDemandes() {

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Demande> modifiedDemandes = demandeRepository.findByUpdatedAtAfter(thirtyDaysAgo);

        return modifiedDemandes.stream()
                .map(DemandeDto::fromEntity)
                .collect(Collectors.toList());
    }



   /* @Override
    public List<String> getModificationsDescriptionsForDemande(Long demandeId) {
        return historiqueRepository.findDescriptionsForDemande(demandeId);
    }*/

    public Page<Demande> getDemands(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return demandeRepository.findAll(pageable);
    }

    public long getTotalDemandes() {
        return demandeRepository.count();
    }

    public void updateStatut(Integer demandeId, StatutEnum statut, String updatedBy) {
        Optional<Demande> optionalDemande = demandeRepository.findById(demandeId);

        if (optionalDemande.isPresent()) {
            Demande demande = optionalDemande.get();

            // Mettre à jour le statut et l'utilisateur qui a effectué la mise à jour
            demande.setStatus(statut);
            demande.setUpdatedBy(updatedBy);
            demandeRepository.save(demande);

            // Créer une notification pour le créateur de la demande
            String message = "Le statut de votre demande a été modifié à " + statut;
            notificationService.createNotification(message, demande.getCreateur());
        } else {
            throw new RuntimeException("Demande not found with id: " + demandeId);
        }
    }


    public long getTotalDemandesCount() {
        return demandeRepository.count();
    }

    public List<Map<String, Object>> getTopDemandes() {
        Pageable pageable = PageRequest.of(0, 3); // Page 0, 3 items
        return demandeRepository.findTopDemandes(pageable);
    }

    public Long getEmployeeIdByDemandeId(Long demandeId) {
        // Récupérer la demande par son ID
        Demande demande = demandeRepository.findById(demandeId.intValue())
                .orElseThrow(() -> new RuntimeException("Demande not found with ID: " + demandeId));

        // Récupérer le matricule de l'employé depuis la demande
        String matricule = demande.getEmployeeMatricule();
        if (matricule == null || matricule.isEmpty()) {
            throw new RuntimeException("Matricule is not specified in the demande");
        }

        // Rechercher l'employé par le matricule
        Employee employee = employeeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));

        // Récupérer l'utilisateur associé à cet employé
        User user = employee.getUser();
        if (user == null) {
            throw new RuntimeException("User not found for employee with matricule: " + matricule);
        }

        // Retourner l'ID de l'utilisateur
        return user.getId();
    }

    public String getEmployeeMatriculeByDemandeId(Long demandeId) {
        // Récupérer la demande par son ID
        Demande demande = demandeRepository.findById(demandeId.intValue())
                .orElseThrow(() -> new RuntimeException("Demande not found with ID: " + demandeId));

        // Récupérer et retourner le matricule de l'employé
        return demande.getEmployeeMatricule();
    }

    public Map<String, Long> countDemandsByCategory() {
        return demandeRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        demande -> Optional.ofNullable(demande.getCategorie())
                                .map(CategorieEnum::toString)
                                .orElse("Autre"),  // Handle null categories by assigning a default value
                        Collectors.counting()
                ));
    }


    public Long findUserIdByDemandeId(Long demandeId) {
        // Find the demande by ID
        Demande demande = demandeRepository.findById(demandeId.intValue())
                .orElseThrow(() -> new RuntimeException("Demande not found with ID: " + demandeId));

        // Retrieve the matricule from the demande
        String matricule = demande.getEmployeeMatricule();

        // Find the user by the employee's matricule
        User user = userRepository.findByEmployeeMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("User not found with matricule: " + matricule));

        // Return the user's ID
        return user.getId();
    }

    public List<DemandeDto> findDemandsByCurrentUser() {
        // Get the currently authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }

        org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

        // Retrieve the user's name from their details (this assumes the username is used as the creator name)
        String createur = userDetailsServiceImp.getMatriculeFromPrincipal(userDetails);

        // Filter and return only the demands created by the authenticated user
        return demandeRepository.findByCreateur(createur)
                .stream()
                .map(DemandeDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DemandeDto> findAllForUser(UserDetails userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Utiliser userService pour obtenir le matricule
        String matricule = userService.getMatriculeFromAuthentication(authentication);

        // Retourner uniquement les demandes liées au matricule de l'utilisateur
        return demandeRepository.findByIsArchivedFalseAndEmployee_Matricule(matricule)
                .stream()
                .map(DemandeDto::fromEntity)
                .collect(Collectors.toList());
    }





    public int countUniqueMatricules(List<DemandeDto> demandes) {
        Set<String> uniqueMatricules = new HashSet<>();
        for (DemandeDto demande : demandes) {
            // Ajoutez le matricule à l'ensemble
            uniqueMatricules.add(demande.getEmployeeMatricule());
        }
        // Retournez le nombre d'éléments uniques
        return uniqueMatricules.size();
    }




}