package com.odk.Controller;

import com.odk.Service.Interface.Service.ReportingService;
import com.odk.dto.ReportingDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reporting")
@AllArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    // Récupérer tous les participants
    @GetMapping("/participants")
    public ResponseEntity<List<ReportingDTO>> getAllParticipants() {
        return ResponseEntity.ok(reportingService.getAllParticipants());
    }

    // Récupérer les participants filtrés par entité et activité
    @GetMapping("/participants/filter")
    public ResponseEntity<List<ReportingDTO>> getFilteredParticipants(
            @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) Long activiteId
    ) {
        return ResponseEntity.ok(reportingService.getParticipantsFiltered(entiteId, activiteId));
    }
}
