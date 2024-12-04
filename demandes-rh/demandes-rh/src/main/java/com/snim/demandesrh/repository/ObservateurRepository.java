package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Observateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObservateurRepository extends JpaRepository<Observateur, Integer> {
}
