package com.snim.demandesrh.entities;

import com.snim.demandesrh.enums.StatutEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class DemandeDrh extends BaseEntity{
    public long id;
    public StatutEnum statut;
    public LocalDateTime createdAt;
    public String createdBy;
    public LocalDateTime updatedAt;
    public String updatedBy;



}
