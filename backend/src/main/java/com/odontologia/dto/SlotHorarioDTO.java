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
public class SlotHorarioDTO {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String horaInicio;
    private String horaFin;
}
