package com.odk.Service.Interface.Service;

import com.odk.Repository.ParticipantRepository;
import com.odk.dto.ReportingDTO;
import com.odk.Entity.Participant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportingService {

    private final ParticipantRepository participantRepository;

    public List<ReportingDTO> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReportingDTO> getParticipantsFiltered(Long entiteId, Long activiteId) {
        return participantRepository.findAll().stream()
                .filter(p -> {
                    boolean okEntite = entiteId == null ||
                            (p.getActivite() != null &&
                                    p.getActivite().getEntite() != null &&
                                    p.getActivite().getEntite().getId().equals(entiteId));

                    boolean okActivite = activiteId == null ||
                            (p.getActivite() != null &&
                                    p.getActivite().getId().equals(activiteId));

                    return okEntite && okActivite;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ReportingDTO mapToDTO(Participant p) {
        return new ReportingDTO(
                p.getNom(),
                p.getPrenom(),
                p.getEmail(),
                p.getPhone(),
                p.getGenre(),
                p.getActivite() != null ? p.getActivite().getNom() : "",
                p.getActivite() != null && p.getActivite().getEntite() != null
                        ? p.getActivite().getEntite().getNom()
                        : "",
                p.getAge(),
                p.getActivite() != null ? p.getActivite().getDateDebut() : null,
                p.getActivite() != null ? p.getActivite().getDateFin() : null
        );
    }
}
