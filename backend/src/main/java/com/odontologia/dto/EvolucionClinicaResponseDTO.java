package com.odontologia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolucionClinicaResponseDTO {
    private Long id;
    private Long historiaClinicaId;
    private Long pacienteId;
    private Long odontologoId;
    private String odontologoNombreCompleto;
    private String odontologoMatricula;
    private Long turnoId;
    private LocalDateTime fechaHora;
    private String diagnostico;
    private String tratamientoRealizado;
    private String observaciones;
}