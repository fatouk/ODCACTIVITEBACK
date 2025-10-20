package com.odk.Controller;

import com.odk.Entity.Etape;
import com.odk.Entity.ResponseMessage;
import com.odk.Repository.EtapeRepository;
import com.odk.Service.Interface.Service.EtapeService;
import com.odk.dto.EtapeDTO;
import com.odk.helper.ExcelHelper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/etape")
@AllArgsConstructor
public class EtapeController {

    private EtapeService etapeService;
    private EtapeRepository etapeRepository;


//    @PostMapping("/ajout")
//    @ResponseStatus(HttpStatus.CREATED)
////    public ResponseEntity<List<Etape>> addEtapes(@RequestBody List<Etape> etapes) {
////        List<Etape> savedEtapes = etapes.stream()
////                .map(etapeRepository::save)
////                .collect(Collectors.toList());
////        return ResponseEntity.ok(savedEtapes);
////    }
///
    @PostMapping
    @PreAuthorize("hasRole('PERSONNEL')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<Etape>> addEtapes(@RequestBody List<Etape> etapes) {
        List<Etape> savedEtapes = etapes.stream()
                .map(etape -> {
                    etape.mettreAJourStatut(); // Mise à jour du statut avant sauvegarde
                    return etapeRepository.save(etape);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(savedEtapes);
    }


    @GetMapping
    @PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    @ResponseStatus(HttpStatus.OK)

    public List<EtapeDTO> getAllEtapes() {
        return etapeService.getAllEtapes(); // Utilise le service pour récupérer les étapes sous forme de DTO
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PERSONNEL')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Etape> Modifier(@PathVariable Long id, @RequestBody Etape etape ){

      Etape updateEtape =  etapeService.update(etape,id);
        return ResponseEntity.ok(updateEtape);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PERSONNEL')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void  supprimer(@PathVariable Long id){
        etapeService.delete(id);
    }

   /* @PostMapping("/{id}/participants/upload")
    @PreAuthorize("hasRole('PERSONNEL')")
    public ResponseEntity<?> uploadParticipants(@PathVariable Long id, @RequestParam("file") MultipartFile file, @RequestParam boolean toListeDebut) {
        try {
            etapeService.addParticipantsToEtape(id, file, toListeDebut);
            return ResponseEntity.ok(new ResponseMessage("Participants ajoutés avec succès"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("Erreur lors de l'importation du fichier"));
        }
    }*/

    @PostMapping("/{id}/participants/upload")
    @PreAuthorize("hasRole('PERSONNEL')")
    public ResponseEntity<?> uploadParticipants(@PathVariable Long id, @RequestParam("file") MultipartFile file, @RequestParam boolean toListeDebut) {
        try {
            // Valider que l'étape peut être modifiée
            etapeService.validateEtapeForModification(id);

            // Procéder à l'ajout des participants
            etapeService.addParticipantsToEtape(id, file, toListeDebut);
            return ResponseEntity.ok(new ResponseMessage("Participants ajoutés avec succès"));

        } catch (EtapeService.EtapeTermineeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseMessage("Impossible d'ajouter des participants : " + e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("Erreur lors de l'importation du fichier"));
        }
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PERSONNEL') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<EtapeDTO>> getEtape(@PathVariable Long id) {
        List<EtapeDTO> etapes = etapeService.getByIdEtapes(id);
        return new ResponseEntity<>(etapes, HttpStatus.OK);
    }

}
