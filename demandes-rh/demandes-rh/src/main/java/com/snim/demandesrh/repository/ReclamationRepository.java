package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Reclamation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Integer> {

    List<Reclamation> findByUpdatedAtAfter(LocalDateTime date);
    Optional<Reclamation> findById(Integer id);
    List<Reclamation> findByCreateur(String createur);

    long count();
    List<Reclamation> findByIsArchivedTrue();
    List<Reclamation> findByIsArchivedFalse();

    @Query("SELECT r.categorie AS categorie, COUNT(r) AS count FROM Reclamation r GROUP BY r.categorie ORDER BY count DESC")
    List<Map<String, Object>> findTopReclamations(Pageable pageable);

    Optional<Reclamation> findByEmployee_Matricule(String matriculeEmploye);
    List<Reclamation> findByIsArchivedFalseAndEmployee_Matricule(String matricule);

}
