package com.snim.demandesrh.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity

public class Document extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    private UUID uuid;
    public String fileName, fileLocation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "demande_id")
    @JsonIgnore
    private Demande demande;


    @ManyToOne
    @JoinColumn(name = "reclamationn_id")
    @JsonIgnore
    private Reclamation reclamation;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private String contentType;

    public Document(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

}
