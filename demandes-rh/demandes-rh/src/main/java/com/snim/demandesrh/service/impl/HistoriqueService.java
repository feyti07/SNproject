package com.snim.demandesrh.service.impl;
import com.snim.demandesrh.entities.Historique;
import com.snim.demandesrh.repository.HistoriqueRepository;
import com.snim.demandesrh.service.IHistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistoriqueService implements IHistoriqueService {

    private final HistoriqueRepository historiqueRepository;

    @Autowired
    public HistoriqueService(HistoriqueRepository historiqueRepository) {
        this.historiqueRepository = historiqueRepository;
    }

    @Override
    public List<Historique> findByType(String type) {
        return historiqueRepository.findByType(type);
    }

    @Override
    public void create(Historique historique) {
        historiqueRepository.save(historique);
    }
    @Override
    public List<Historique> getHistoriqueByDemandeId(Long demandeId) {
        return historiqueRepository.findByDemandeId(demandeId);
    }
}
