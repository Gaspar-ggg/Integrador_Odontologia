package com.odontologia.dto;

import com.odontologia.model.EstadoTurno;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {

    @NotNull(message = "El identificador del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El identificador del odontólogo es obligatorio")
    private Long odontologoId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime fechaHoraInicio;

    private LocalDateTime fechaHoraFin;

    @Size(max = 1000, message = "El motivo de consulta no puede superar los 1000 caracteres")
    private String motivoConsulta;

    private EstadoTurno estado;
}
