package com.snim.demandesrh.service;

import com.snim.demandesrh.entities.Historique;
import com.snim.demandesrh.entities.dto.HistoriqueDto;

import java.util.List;

public interface IHistoriqueService {
    List<Historique> findByType(String type);

    void create(Historique historique);

    public List<Historique> getHistoriqueByDemandeId(Long demandeId);
}
