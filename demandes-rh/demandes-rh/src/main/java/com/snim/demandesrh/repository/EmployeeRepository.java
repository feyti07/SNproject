package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByMatricule(String matricule);


    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByUserUsername(String username);

    Optional<Employee> findByUserEmail(String email);



}
