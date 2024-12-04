package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByDemandeId(Long demandeId);
    Document findByDemandeIdAndFileName(Long demandeId, String fileName);
    Optional<Document> findByUuid(UUID uuid);

    List<Document> findByReclamationId(Long reclamationId);





}
