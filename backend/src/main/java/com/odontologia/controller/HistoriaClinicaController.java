package com.odontologia.controller;

import com.odontologia.dto.EvolucionClinicaRequestDTO;
import com.odontologia.dto.EvolucionClinicaResponseDTO;
import com.odontologia.dto.HistoriaClinicaDetalleResponseDTO;
import com.odontologia.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/historias-clinicas")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    @GetMapping("/paciente/{id}")
    @PreAuthorize("hasAnyRole('ODONTOLOGO', 'ADMIN', 'RECEPCION')")
    public ResponseEntity<HistoriaClinicaDetalleResponseDTO> obtenerPorPaciente(@PathVariable Long id) {
        return ResponseEntity.ok(historiaClinicaService.obtenerHistoriaClinicaPorPaciente(id));
    }

    @PostMapping("/paciente/{id}/evoluciones")
    @PreAuthorize("hasRole('ODONTOLOGO')")
    public ResponseEntity<EvolucionClinicaResponseDTO> registrarEvolucion(
            @PathVariable Long id,
            @Valid @RequestBody EvolucionClinicaRequestDTO request,
            Authentication authentication) {
        EvolucionClinicaResponseDTO creada = historiaClinicaService.registrarEvolucion(
                id, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}