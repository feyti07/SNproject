package com.snim.demandesrh.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseUser{
    public String matricule, username, email, uoCode, uoText;
    public String positionCode, positionText, password;
    public boolean active;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGTEXT")
    private String photo;








}
