package com.snim.demandesrh.service;

import com.snim.demandesrh.entities.Reclamation;
import com.snim.demandesrh.entities.dto.DemandeDto;
import com.snim.demandesrh.entities.dto.ReclamationDto;
import com.snim.demandesrh.enums.StatutEnum;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface IReclamationService extends AbstractService<ReclamationDto> {
    void updateStatut(Long reclamationId, StatutEnum statut);
    ReclamationDto update(Long reclamationId, ReclamationDto reclamationDto);
    List<ReclamationDto> getModifiedReclamations();

    /* List<String> getModificationsDescriptionsForReclamation(Long reclamationId); */
    void delete(Long id); // Ensure this method is defined here
    ReclamationDto findById(Long id); // Ensure this method is defined here

    public List<ReclamationDto> findAllForUser(UserDetails userDetails) ;

    ReclamationDto update(Integer reclamationId, ReclamationDto reclamationDto);//
}
