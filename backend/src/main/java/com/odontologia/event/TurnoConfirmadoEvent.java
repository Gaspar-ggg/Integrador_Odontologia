package com.odontologia.event;

import java.time.LocalDateTime;

public record TurnoConfirmadoEvent(
        Long turnoId,
        String pacienteNombreCompleto,
        String pacienteEmail,
        String pacienteTelefono,
        String odontologoNombreCompleto,
        String odontologoEspecialidad,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String motivoConsulta
) {
}