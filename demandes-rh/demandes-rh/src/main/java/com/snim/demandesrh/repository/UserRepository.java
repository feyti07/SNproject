package com.snim.demandesrh.repository;

import com.snim.demandesrh.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByRoleName(String roleName);
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findAllByRoles(@Param("roles") List<String> roles);




    Optional<User> findByEmployeeMatricule(String matricule);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    Optional<User> findByMatricule(String matricule);


    @Query("SELECT u.id FROM User u WHERE u.employee.matricule = :matricule")
    Long findUserIdByMatricule(@Param("matricule") String matricule);







}
