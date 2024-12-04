package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.dto.DemandeDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Integer> {

    List<Demande> findByUpdatedAtAfter(LocalDateTime date);
    Optional<Demande> findById(Integer id);
    List<Demande> findByCreateur(String createur);


    long count();
    List<Demande> findByIsArchivedTrue();
    List<Demande> findByIsArchivedFalse();
    @Query("SELECT d.categorie AS categorie, COUNT(d) AS count FROM Demande d GROUP BY d.categorie ORDER BY count DESC")
    List<Map<String, Object>> findTopDemandes(Pageable pageable);

    Optional<Demande> findByMatriculeEmploye(String matriculeEmploye);
    List<Demande> findByIsArchivedFalseAndEmployee_Matricule(String matricule);

    List<Demande> findByIsArchivedFalse(Sort sort); // Pour les demandes non archivées
    List<Demande> findByIsArchivedFalseAndEmployee_Matricule(String matricule, Sort sort);

}

