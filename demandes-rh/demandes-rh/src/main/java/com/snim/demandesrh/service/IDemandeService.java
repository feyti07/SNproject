package com.snim.demandesrh.service;

import com.snim.demandesrh.entities.Demande;
import com.snim.demandesrh.entities.dto.DemandeDto;
import com.snim.demandesrh.enums.StatutEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface IDemandeService extends AbstractService<DemandeDto>{
    public void updateStatut(Integer demandeId, StatutEnum statut, String updatedBy);

    DemandeDto update(Integer demandeId, DemandeDto demandeDto);
    public List<DemandeDto> getModifiedDemandes();

   /* List<String> getModificationsDescriptionsForDemande(Long demandeId);*/
   Optional<Demande> findById(long id);



    long saveDemande(DemandeDto demandeDto, MultipartFile pieceJointe, String customFilePath, UserDetails userDetails) throws IOException;

    public List<DemandeDto> findAllForUser(UserDetails userDetails) ;


}
