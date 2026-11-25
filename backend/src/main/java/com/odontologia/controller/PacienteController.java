package com.odontologia.controller;

import com.odontologia.dto.PacienteRequestDTO;
import com.odontologia.dto.PacienteResponseDTO;
import com.odontologia.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crearPaciente(request));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<PacienteResponseDTO>> buscarPacientes(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(pacienteService.buscarPacientes(dni, q));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}
