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
public class HistoriaClinicaResponseDTO {
    private Long id;
    private Long pacienteId;
    private LocalDateTime fechaCreacion;
    private String observacionesGenerales;
}
