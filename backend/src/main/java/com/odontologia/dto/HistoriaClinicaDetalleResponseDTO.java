package com.odontologia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinicaDetalleResponseDTO {
    private Long id;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private String pacienteDni;
    private LocalDateTime fechaCreacion;
    private String observacionesGenerales;
    private List<EvolucionClinicaResponseDTO> evoluciones;
}