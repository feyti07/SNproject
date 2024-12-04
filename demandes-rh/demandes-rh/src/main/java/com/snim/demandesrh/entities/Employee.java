package com.snim.demandesrh.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity

public class Employee extends BaseUser  {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public String getName() {
        return username;
    }






}
