package com.odk.dto;

import com.odk.Entity.Activite;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ParticipantDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String phone;
    private String genre;
    private Activite activite; // Nom de l'activité complet
    private boolean checkedIn;
    private LocalDateTime checkInTime;
    private Integer age; // nouveau champ ajouté

    // Constructeur depuis l'entité Participant
    public ParticipantDTO(com.odk.Entity.Participant participant) {
        this.id = participant.getId();
        this.nom = participant.getNom();
        this.prenom = participant.getPrenom();
        this.email = participant.getEmail();
        this.phone = participant.getPhone();
        this.genre = participant.getGenre();
        this.activite = participant.getActivite();
        this.checkedIn = participant.isCheckedIn();
        this.checkInTime = participant.getCheckInTime();
        this.age = participant.getAge(); // récupère l'âge de la base
    }

    // Constructeur minimal
    public ParticipantDTO(Long id, String nom) {
        this.id = id;
        this.nom = nom;
    }
}
