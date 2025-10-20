package com.odk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.odk.Entity.Critere;
import com.odk.Enum.Statut;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class EtapeDTO {

    private Long id;
    private String nom;
    private Statut statut;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateDebut;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateFin;
//    private ActiviteDTO activite;
    private List<CritereDTO> critere;
    private List<ParticipantDTO> listeDebut;
    private List<ParticipantDTO> listeResultat;


    public EtapeDTO() {
        this.listeDebut = new ArrayList<>();
        this.listeResultat = new ArrayList<>();
    }

}
