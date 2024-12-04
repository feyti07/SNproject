package com.snim.demandesrh.entities.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {

    private String email;
    private String password;
}

