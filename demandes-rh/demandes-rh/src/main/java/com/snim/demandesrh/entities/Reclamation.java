package com.snim.demandesrh.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.snim.demandesrh.enums.ImpactEnum;
import com.snim.demandesrh.enums.StatutEnum;
import com.snim.demandesrh.enums.TypeEnum;
import com.snim.demandesrh.enums.UrgenceEnum;
import com.snim.demandesrh.enums.ReclamationEnums.CategorieEnum;
import com.snim.demandesrh.enums.ReclamationEnums.LieuEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.snim.demandesrh.enums.DemandeEnums.CentreEnum;
import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@Entity
public class Reclamation extends DemandeDrh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private CentreEnum center;
    private LieuEnum lieu;
    private CategorieEnum categorie;
    private UrgenceEnum urgence;
    private ImpactEnum impact;
    private String source;
    public String description;

    private TypeEnum type;
    private StatutEnum status; // Défaut à NOUVEAU
    private LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    private String createur;
    public String updatedBy;
    private String matriculeEmploye;

    @Lob
    private byte[] pieceJointe;
    private boolean isArchived = false;

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    @PreUpdate
    protected void onUpdate() {
        // Mettre à jour la date de modification
        this.updatedAt = LocalDateTime.now();
        System.out.println("Votre message ici");
        // Créer un historique pour la mise à jour de la réclamation
        createHistorique("Modification de la réclamation", "Modification", LocalDateTime.now(), "Nom de l'acteur");
    }

    private void createHistorique(String description, String type, LocalDateTime date, String acteurName) {
        Historique historique = new Historique();
        if (this.historiques == null) {
            this.historiques = new HashSet<>();
        }
        historique.setDescription(description);
        historique.setType(type);
        historique.setDate(date);
        historique.setActeurName(acteurName);
        historique.setReclamation(this);
        this.historiques.add(historique);
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "acteurName")
    private Intervenant intervenant;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Document> piecesJointes = new ArrayList<>();

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Historique> historiques = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Observateur> observateurs = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    public String getEmployeeMatricule() {
        return employee != null ? employee.getMatricule() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reclamation reclamation = (Reclamation) o;
        return Objects.equals(id, reclamation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @OneToMany(mappedBy = "reclamation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Message> messages = new LinkedHashSet<>();
}
