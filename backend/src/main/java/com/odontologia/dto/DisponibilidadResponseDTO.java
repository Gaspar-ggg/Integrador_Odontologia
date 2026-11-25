package com.odontologia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadResponseDTO {
    private Long odontologoId;
    private String odontologoNombreCompleto;
    private LocalDate fecha;
    private int duracionMinutos;
    private List<SlotHorarioDTO> slotsDisponibles;
}
