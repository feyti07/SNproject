package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.EmployeeDto;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.repository.EmployeeRepository;
import com.snim.demandesrh.service.impl.DemandeService;
import com.snim.demandesrh.service.impl.EmployeService;
import com.snim.demandesrh.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.stream.Collectors;


import java.io.IOException;


@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeService employeService;

    @Autowired
    private UserService userService;

    @Autowired
    private DemandeService demandeService;

    @GetMapping
    public List<EmployeeDto> getAllEmployees() {
        return employeService.findAllEmployees(); // Adjust method as needed
    }

    @GetMapping("/matricules")
    public List<String> getAllEmployeeMatricules() {
        return employeeRepository.findAll().stream()
                .map(Employee::getMatricule)
                .collect(Collectors.toList());
    }


    @GetMapping("/d/{id}")
    public ResponseEntity<?> getDemandeById(@PathVariable Long id) {
        Demande demande = demandeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande not found"));

        // Get the matricule from the demande
        String matricule = demande.getEmployeeMatricule();

        // Fetch the employee by matricule
        Employee employee = employeService.findByMatriculeEmp(matricule)
                .orElseThrow(() -> new RuntimeException("Employee not found with matricule: " + matricule));

        // Return the demande with the employee name
        return ResponseEntity.ok("Demande for employee: " + employee.getUsername());
    }



    @GetMapping("/current")
    public ResponseEntity<String> getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();  // Récupérer l'email

        // Trouver l'Employé par l'email de l'utilisateur
        Optional<Employee> employee = employeeRepository.findByUserEmail(email);

        return employee.map(Employee::getUsername)  // Récupérer le nom de l'employé
                .map(name -> ResponseEntity.ok(name))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nom d'utilisateur non trouvé"));
    }




    @PostMapping("/update-photo")
    public ResponseEntity<Void> updatePhoto(@RequestParam("photo") MultipartFile photo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = ((User) userDetails).getId(); // Assurez-vous que votre UserDetails implémente getId()
            Optional<Employee> employeeOptional = employeService.findByUserId(userId);
            if (employeeOptional.isPresent()) {
                Employee employee = employeeOptional.get();
                try {
                    byte[] photoBytes = photo.getBytes();
                    String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);
                    employee.setPhoto(photoBase64);
                    employeeRepository.save(employee);
                    return ResponseEntity.ok().build();
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/current-employee")
    public ResponseEntity<EmployeeDto> getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = ((User) userDetails).getId(); // Assurez-vous que votre UserDetails implémente getId()
            return employeService.findByUserId(userId)
                    .map(employee -> {
                        EmployeeDto employeeDto = EmployeeDto.fromEntity(employee);
                        return ResponseEntity.ok(employeeDto);
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @GetMapping("/profile-photo")
    public ResponseEntity<String> getProfilePhoto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = ((User) userDetails).getId(); // Assurez-vous que votre UserDetails implémente getId()
            Optional<Employee> employeeOptional = employeService.findByUserId(userId);
            if (employeeOptional.isPresent()) {
                Employee employee = employeeOptional.get();
                String photoBase64 = employee.getPhoto();
                return ResponseEntity.ok(photoBase64);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable("id") Long id) {
        Optional<EmployeeDto> employeeDto = employeService.findDtoById(id);
        if (employeeDto.isPresent()) {
            return ResponseEntity.ok(employeeDto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/username/{id}")
    public ResponseEntity<String> getUsernameById(@PathVariable("id") Long id) {
        String username = employeService.getUsernameById(id);
        if (username != null) {
            return ResponseEntity.ok(username);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
        }
    }


    @GetMapping("/username")
    public String getNameByUsername(@RequestParam String username) {
        return employeService.getNameByUsername(username);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto employeeDto) {
        // Assurez-vous que l'ID est correct
        employeeDto.setId(id);
        EmployeeDto updatedEmployee = employeService.updateEmployee(employeeDto);
        return ResponseEntity.ok(updatedEmployee);
    }






}
