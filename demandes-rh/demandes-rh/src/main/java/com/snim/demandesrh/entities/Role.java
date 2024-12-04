package com.snim.demandesrh.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private String name;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user")
    @JsonIgnore
    private User user;


    public Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';  // Ne pas inclure "user" ici
    }

}


