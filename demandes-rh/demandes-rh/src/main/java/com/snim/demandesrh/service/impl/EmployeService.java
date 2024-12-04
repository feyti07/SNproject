package com.snim.demandesrh.service.impl;
import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.EmployeeDto;
import com.snim.demandesrh.repository.EmployeeRepository;
import com.snim.demandesrh.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.snim.demandesrh.entities.Employee;

@Service
@RequiredArgsConstructor
public class EmployeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    private final UserRepository userRepository;




    public List<EmployeeDto> findAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employee -> new EmployeeDto(employee)) // Assuming you have a constructor or method for this
                .collect(Collectors.toList());
    }

    public Optional<Employee> findByMatriculeEmp(String matricule) {
        return employeeRepository.findByMatricule(matricule);
    }

    public List<String> getEmployeeMatricules() {
        return employeeRepository.findAll().stream()
                .map(Employee::getMatricule) // Assurez-vous que `getMatricule` est une méthode de votre entité Employee
                .collect(Collectors.toList());
    }

    public Optional<Employee> findByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }



    public Employee getCurrentUserEmployee() {
        Long userId = userService.getCurrentUserId();
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for current user"));
    }

    public Employee findByMatricule(String matricule) {
        return employeeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new EntityNotFoundException("Employé avec le matricule " + matricule + " introuvable"));
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    // Méthode pour convertir l'Entity en DTO
    public Optional<EmployeeDto> findDtoById(Long id) {
        return employeeRepository.findById(id).map(EmployeeDto::fromEntity);
    }

    public String getCurrentUserName() {
        // Get the currently authenticated user's username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "Utilisateur non authentifié";
        }

        String currentUsername = authentication.getName();

        // Find the Employee by the username of the associated User
        Optional<Employee> employee = employeeRepository.findByUserUsername(currentUsername);

        // Return the Employee's name or a default message if not found
        return employee.map(Employee::getUsername).orElse("Nom d'utilisateur non trouvé");
    }

    public String getUsernameById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id); // Assurez-vous d'avoir un EmployeeRepository avec cette méthode
        return employee.map(Employee::getUsername).orElse(null);
    }


    public String getNameByUsername(String username) {
        // Find the User by username
        Optional<User> user = userRepository.findByUsername(username);

        // Return the User's name using the getName() method, or a default message if not found
        return user.map(User::getName).orElse("Nom d'utilisateur non trouvé");
    }

    public EmployeeDto updateEmployee(EmployeeDto employeeDto) {
        Employee existingEmployee = employeeRepository.findById(employeeDto.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Mettez à jour les propriétés de l'employé existant avec celles de l'objet DTO
        existingEmployee.setUsername(employeeDto.getName());
        existingEmployee.setEmail(employeeDto.getEmail());
        existingEmployee.setPositionText(employeeDto.getPositionText());
        existingEmployee.setUoText(employeeDto.getUoText());

        // Sauvegarder et retourner le nouvel employé
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return EmployeeDto.fromEntity(updatedEmployee);
    }


}