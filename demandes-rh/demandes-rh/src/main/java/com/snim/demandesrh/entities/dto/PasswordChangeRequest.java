package com.snim.demandesrh.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class PasswordChangeRequest {
    //private String username;
    private String oldPassword;
    private String newPassword;
}
