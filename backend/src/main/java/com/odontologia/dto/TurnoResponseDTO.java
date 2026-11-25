package com.odontologia.dto;

import com.odontologia.model.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {
    private Long id;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private Long odontologoId;
    private String odontologoNombreCompleto;
    private String odontologoEspecialidad;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private EstadoTurno estado;
    private String motivoConsulta;
    private Boolean activo;
}
