package com.odk.Service.Interface.Service;

import com.odk.Entity.Activite;
import com.odk.Entity.Salle;
import com.odk.Entity.Utilisateur;
import com.odk.Enum.Statut;
import com.odk.Repository.ActiviteRepository;
import com.odk.Repository.SalleRepository;
import com.odk.Repository.UtilisateurRepository;
import com.odk.Service.Interface.CrudService;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ActiviteService implements CrudService<Activite, Long> {

    private ActiviteRepository activiteRepository;
    private PersonnelService personnelService;
    private EmailService emailService;
    private UtilisateurService utilisateurService;
    private UtilisateurRepository utilisateurRepository;
     private SalleRepository salleRepository;

    @Override
    public Activite add(Activite entity) {
        try {
            System.out.println("ajout type=================="+entity.getTypeActivite().getId());
            // Récupérer l'utilisateur connecté
            String email1 = SecurityContextHolder.getContext().getAuthentication().getName();
            Utilisateur utilisateurPerso = utilisateurRepository.findByEmail(email1)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

            // Associer l'utilisateur comme créateur
            entity.setCreatedBy(utilisateurPerso);
            
             List<Activite> nomconflits = activiteRepository.findConflictingNomActivites(entity.getNom(),entity.getDateDebut(),
                    entity.getDateFin(),
                    Statut.Termine // Passer l'énumération Statut.Termine
            );

            if (!nomconflits.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Le nom de cette activité est déjà crée avec les memes dates.");
            }

            List<Activite> conflits = activiteRepository.findConflictingActivites(
                    entity.getSalleId().getId(),
                    entity.getDateDebut(),
                    entity.getDateFin(),
                    Statut.Termine // Passer l'énumération Statut.Termine
            );

            if (!conflits.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "La salle est déjà réservée pour une activité en cours ou en attente.");
            }

            // Mettre à jour le statut de l'activité
            entity.mettreAJourStatut();
            // Enregistrer l'activité
            Activite activiteCree = activiteRepository.save(entity);

            envoiMail(activiteCree);

            return activiteCree;
        } catch (DataAccessException e) {
            e.printStackTrace(); // Pour afficher l'exception complète
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Le nom de cette activité est déjà crée avec les memes dates.", e);
        } catch (Exception e) {
            e.printStackTrace(); // Pour afficher l'exception complète
           throw new ResponseStatusException(HttpStatus.CONFLICT, "La salle est déjà réservée.", e);
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une erreur dans le processus.", e);
        }
//        catch (Exception ee) {
//            ee.printStackTrace(); // Pour afficher l'exception complète
//           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur est survenue. Veuillez réessayer.", e);
//        }
    }
public void envoiMail(Activite activiteCree){
    // Récupérer la liste des utilisateurs
    Date dateDebut = activiteCree.getDateDebut();
    Date dateFin = activiteCree.getDateFin();
    SimpleDateFormat form= new SimpleDateFormat("dd/MM/yyyy");    
    String date1=form.format(dateDebut);
     String date2=form.format(dateFin);
     Salle s=salleRepository.findById(activiteCree.getSalleId().getId()).get();
    
     String salle=s.getNom();
    System.err.println("la salle mail====="+ salle);
            List<Utilisateur> utilisateurs = utilisateurService.List(); // Assurez-vous d'avoir cette méthode

             //  Filtrer les utilisateurs ayant le rôle "personnel"
            List<String> emailsPersonnel = utilisateurs.stream()
                    .filter(utilisateur -> utilisateur.getRole().getNom().equals("PERSONNEL")) // Vérifiez que le rôle est bien défini
                    .map(Utilisateur::getEmail) // Récupérer les emails
                    .collect(Collectors.toList());
            // Construire le corps de l'email avec HTML pour une meilleure présentation
            StringBuilder emailBodyBuilder = new StringBuilder();
            emailBodyBuilder.append("<!DOCTYPE html>");
            emailBodyBuilder.append("<html lang=\"fr\">");
            emailBodyBuilder.append("<head>");
            emailBodyBuilder.append("<meta charset=\"UTF-8\">");
            emailBodyBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            emailBodyBuilder.append("<title>Nouvelle Activité Créée</title>");
            emailBodyBuilder.append("<style>");
            emailBodyBuilder.append("  body { font-family: Arial, sans-serif; background-color: #f39c12; margin: 0; padding: 20px; }");
            emailBodyBuilder.append("  .container { background-color: #ffffff; padding: 20px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
            emailBodyBuilder.append("  .header { text-align: center; padding-bottom: 20px; }");
            emailBodyBuilder.append("  .content { line-height: 1.6; }");
            emailBodyBuilder.append("  .footer { margin-top: 20px; font-size: 0.9em; color: #555555; text-align: center; }");
            emailBodyBuilder.append("</style>");
            emailBodyBuilder.append("</head>");
            emailBodyBuilder.append("<body>");
            emailBodyBuilder.append("<div class=\"container\">");
            emailBodyBuilder.append("<div class=\"header\">");
            emailBodyBuilder.append("<h2>Nouvelle Activité Créée</h2>");
            emailBodyBuilder.append("</div>");
            emailBodyBuilder.append("<div class=\"content\">");
            emailBodyBuilder.append("<p>Bonjour,</p>");
            emailBodyBuilder.append("<p>Une nouvelle activité a été créée dans notre système.</p>");
            emailBodyBuilder.append("<p><strong>Nom de l'activité :</strong> ").append(activiteCree.getNom()).append("</p>");
            emailBodyBuilder.append("<p><strong>Description :</strong> ").append(activiteCree.getDescription()).append("</p>");
            emailBodyBuilder.append("<p><strong>Date du:</strong> ").append(date1).append(" AU: ").append(date2).append("</p>");
            emailBodyBuilder.append("<p><strong>Dans la Salle :</strong> ").append(salle).append("</p>");
            emailBodyBuilder.append("<p>Nous vous invitons à consulter cette activité pour plus de détails.</p>");
            emailBodyBuilder.append("</div>");
            emailBodyBuilder.append("<div class=\"footer\">");
            emailBodyBuilder.append("<p>L'équipe <strong>ODC</strong></p>");
            emailBodyBuilder.append("<p>Ceci est un email automatisé. Merci de ne pas y répondre.</p>");
            emailBodyBuilder.append("</div>");
            emailBodyBuilder.append("</div>");
            emailBodyBuilder.append("</body>");
            emailBodyBuilder.append("</html>");

            String emailBody = emailBodyBuilder.toString();
            String sujet = "Nouvelle Activité Créée: " + activiteCree.getNom();
emailService.sendSimpleEmail("fatoumata.KALOGA@orangemali.com", sujet, emailBody);
// Envoyer un email HTML à chaque utilisateur ayant le rôle "personnel"
            for (String email : emailsPersonnel) {
                //emailService.sendSimpleEmail(email, sujet, emailBody);
            }
}

    @Override
    public List<Activite> List() {
        return activiteRepository.findAll();
    }

    public List<Activite> list() {
        // Récupérer toutes les activités
        List<Activite> activites = activiteRepository.findAll();

        // Filtrer les activités dont l'étape a le statut 'EN_COURS'
        List<Activite> activitesEnCours = activites.stream()
                .filter(activite -> false) // Vérifie le statut
                .collect(Collectors.toList());

        return activitesEnCours;
    }

    @Override
    public Optional<Activite> findById(Long id) {
        return activiteRepository.findById(id);
    }

    @Transactional
    @Override
    public Activite update(Activite activite, Long id) {
        // Récupérer l'utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        return activiteRepository.findById(id).map(a -> {
            // Vérifier que l'utilisateur connecté est le créateur de l'activité
            if (!a.getCreatedBy().getEmail().equals(utilisateur.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Vous n'êtes pas autorisé à modifier cette activité.");
            }

            // Mettre à jour les champs modifiables
            if (activite.getNom() != null) {
                a.setNom(activite.getNom());
            }
            if (activite.getTitre() != null) {
                a.setTitre(activite.getTitre());
            }
            if (activite.getDescription() != null) {
                a.setDescription(activite.getDescription());
            }
            if (activite.getDateDebut() != null) {
                a.setDateDebut(activite.getDateDebut());
            }
            if (activite.getLieu() != null) {
                a.setLieu(activite.getLieu());
            }
            if (activite.getObjectifParticipation() != null) {
                a.setObjectifParticipation(activite.getObjectifParticipation());
            }
            if (activite.getEntite() != null) {
                a.setEntite(activite.getEntite());
            }
            if (activite.getTypeActivite() != null) {
                a.setTypeActivite(activite.getTypeActivite());
            }
            if (activite.getSalleId() != null) {
                a.setSalleId(activite.getSalleId());
            }
            // DES MODIFICATION AFFAIRE ICI 
            if (activite.getEtapes() != null) {
                a.getEtapes().clear();
                a.getEtapes().addAll(activite.getEtapes());
            }

           /* // Mettre à jour les étapes
            updateEtapes(a, activite.getEtapes());*/

            // Mettre à jour le statut
            a.mettreAJourStatut();

            // Sauvegarder les modifications
            return activiteRepository.save(a);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "L'activité avec l'ID spécifié n'existe pas."));
    }

 /*   private void updateEtapes(Activite activite, List<Etape> nouvellesEtapes) {
        // Supprimer les étapes qui ne sont plus associées
        // Ajouter les nouvelles étapes
        activite.getEtapes().addAll(nouvellesEtapes);
    }*/


    @Override
    public void delete(Long id) {
        // Récupérer l'utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
        System.out.println("com.odk.Service.Interface.Service.ActiviteService.delete()");
        // Récupérer l'activité
        Activite activite1 = activiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Activité non trouvée"));
        System.out.println("com.delete()======="+activite1.getCreatedBy().getId().equals(utilisateur.getId()));

        // Vérifier si l'utilisateur est le créateur
        if (!activite1.getCreatedBy().getId().equals(utilisateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à supprimer cette activité");
        }

        Optional<Activite> activiteOptional = activiteRepository.findById(id);
        activiteOptional.ifPresent(activite -> activiteRepository.delete(activite));
    }
    
    
    public List<Activite> getActivitesBySuperviseur(Long superviseurId) {
        System.out.println("je suisssssssss dans fonction===="+superviseurId);       
        return activiteRepository.findBySuperviseurIdOrNull(superviseurId);    

}
   public List<Activite> getActivitesBySuperviseurAttente(Long superviseurId) {
    return activiteRepository.findAttenteBySuperviseurInValidation(superviseurId);
} 
}
