package com.odontologia.controller;

import com.odontologia.dto.OdontologoPublicDTO;
import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.dto.OdontologoResponseDTO;
import com.odontologia.service.OdontologoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OdontologoController {

    private final OdontologoService odontologoService;

    @GetMapping("/public/odontologos")
    public List<OdontologoPublicDTO> catalogoPublico() {
        return odontologoService.catalogoPublico();
    }

    @PostMapping("/admin/odontologos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OdontologoResponseDTO> crear(@Valid @RequestBody OdontologoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(odontologoService.crear(request));
    }

    @PutMapping("/admin/odontologos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OdontologoResponseDTO> modificar(@PathVariable Long id,
                                                            @Valid @RequestBody OdontologoRequestDTO request) {
        return ResponseEntity.ok(odontologoService.modificar(id, request));
    }

    @DeleteMapping("/admin/odontologos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> baja(@PathVariable Long id) {
        odontologoService.baja(id);
        return ResponseEntity.noContent().build();
    }
}
