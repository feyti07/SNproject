package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Intervenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntervenantRepository extends JpaRepository<Intervenant, Integer> {
    Optional<Intervenant> findByMatricule(String matricule);
}
