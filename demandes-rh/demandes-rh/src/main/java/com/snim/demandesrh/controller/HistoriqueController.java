package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.Historique;
import com.snim.demandesrh.service.impl.HistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historiques")
public class HistoriqueController {

    @Autowired
    private HistoriqueService historiqueService;

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<List<Historique>> getHistoriqueByDemandeId(@PathVariable Long demandeId) {
        return ResponseEntity.ok(historiqueService.getHistoriqueByDemandeId(demandeId));
    }
}