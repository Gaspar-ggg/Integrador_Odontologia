package com.odontologia.controller;

import com.odontologia.dto.DisponibilidadResponseDTO;
import com.odontologia.dto.TurnoRequestDTO;
import com.odontologia.dto.TurnoResponseDTO;
import com.odontologia.model.EstadoTurno;
import com.odontologia.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping("/odontologos/{id}/disponibilidad")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisponibilidadResponseDTO> obtenerDisponibilidad(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.obtenerDisponibilidad(id, fecha));
    }

    @PostMapping("/turnos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'PACIENTE')")
    public ResponseEntity<TurnoResponseDTO> crearTurno(@Valid @RequestBody TurnoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crearTurno(request));
    }

    @PutMapping("/turnos/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'ODONTOLOGO', 'PACIENTE')")
    public ResponseEntity<TurnoResponseDTO> cancelarTurno(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.cancelarTurno(id));
    }

    @PutMapping("/turnos/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<TurnoResponseDTO> confirmarTurno(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.confirmarTurno(id));
    }

    @GetMapping("/turnos")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'ODONTOLOGO', 'PACIENTE')")
    public ResponseEntity<List<TurnoResponseDTO>> listarTurnos(
            @RequestParam(required = false) Long odontologoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoTurno estado) {
        return ResponseEntity.ok(turnoService.listarTurnos(odontologoId, pacienteId, fecha, estado));
    }
}
