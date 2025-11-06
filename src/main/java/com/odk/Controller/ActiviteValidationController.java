/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.odk.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odk.Entity.ActiviteValidation;
import com.odk.Entity.Utilisateur;
import com.odk.Repository.ActiviteValidationRepository;
import com.odk.Service.Interface.Service.ActiviteValidationService;
import com.odk.dto.ActiviteValidationDTO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author kaloga081009
 */
@RestController
@RequestMapping("/activitevalidation")
@CrossOrigin(origins = "http://localhost:4200")
public class ActiviteValidationController {
  @Autowired
    private ActiviteValidationService activiteValidationService;
    @Autowired
    private ActiviteValidationRepository activiteValidationRepository;
  @Autowired
  private ObjectMapper objectMapper;
  
    @GetMapping("/HELLO")    
    public String hello(){
        return "bonjour";
    }
    // Créer une validation avec fichier
    
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    public ResponseEntity<?> createValidation(
            @RequestPart("validation") String validationJson,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier) {

        try {        
            // Convertir la chaîne JSON reçue en DTO
            ActiviteValidationDTO dto = objectMapper.readValue(validationJson, ActiviteValidationDTO.class);
               dto.setDate(new Date()); 
            return ResponseEntity.ok(activiteValidationService.ajouterValidation(dto, fichier));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erreur interne : " + e.getMessage()));
        }
    }

    // Liste toutes les validations
    @GetMapping
    //@PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    public List<ActiviteValidationDTO> listeValidations() {
        return activiteValidationService.listeValidations();
    }

    // Récupérer une validation par ID
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    public ActiviteValidationDTO getValidation(@PathVariable Long id) {
        return activiteValidationService.getValidation(id);
    }

    // Télécharger le fichier d'une validation
        @GetMapping("/{id}/fichier")
           //@PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    public ResponseEntity<byte[]> telechargerFichier(@PathVariable Long id) {
    ActiviteValidation validation = activiteValidationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Validation non trouvée"));

    byte[] fichier = validation.getFichierChiffre(); // @Lob, contenu réel du fichier
    String nomFichier = validation.getFichierjoint(); // nom du fichier original

    if (fichier == null || fichier.length == 0) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
        .header("Access-Control-Expose-Headers", "Content-Disposition")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(fichier);
}
    
    @DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable Long id) {
    try {
        if (!activiteValidationRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("message", "Entité introuvable"));
        }

        activiteValidationRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Validation supprimée avec succès"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("message", "Erreur interne : " + e.getMessage()));
    }
}

}
