package com.snim.demandesrh.entities.dto;

import com.snim.demandesrh.entities.Employee;
import com.snim.demandesrh.entities.Role;
import com.snim.demandesrh.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String matricule;
    private String username;
    private String email;
    private String uoCode;
    private String uoText;
    private String positionCode;
    private String positionText;
    private boolean active;
    private String roleName;
    private String photo;

    public EmployeeDto(Employee employee) {
        this.id = employee.getId(); // Ensure this method exists
        this.username = employee.getUsername();
        this.email = employee.getEmail();
        this.positionText = employee.getPositionText();
    }

    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .matricule(employee.getMatricule())
                .username(employee.getUser().getName())
                .email(employee.getUser().getEmail())
                .uoCode(employee.getUoCode())
                .uoText(employee.getUoText())
                .positionCode(employee.getPositionCode())
                .positionText(employee.getPositionText())
                .active(employee.isActive())
                .photo(employee.getPhoto())
                .build();
    }

    public static Employee toEntity(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setMatricule(dto.getMatricule());
        employee.setUsername(dto.getName());
        employee.getUser().setEmail(dto.getEmail());
        employee.setUoCode(dto.getUoCode());
        employee.setUoText(dto.getUoText());
        employee.setPositionCode(dto.getPositionCode());
        employee.setPositionText(dto.getPositionText());
        employee.setActive(dto.isActive());
        employee.setPhoto(dto.getPhoto());
        return employee;
    }

    public String getName() {
        return username;
    }
    public void setName(){
        this.username = username;
    }
}