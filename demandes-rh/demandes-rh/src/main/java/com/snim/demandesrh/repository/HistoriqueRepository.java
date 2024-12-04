package com.snim.demandesrh.repository;


import com.snim.demandesrh.entities.Historique;
import com.snim.demandesrh.entities.dto.HistoriqueDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Integer> {
    List<Historique> findByType(String type);

    @Query("SELECT h.description FROM Historique h WHERE h.demande.id = :demandeId")
    List<String> findDescriptionsForDemande(Long demandeId);

    public List<Historique> findByDemandeId(Long demandeId);
}