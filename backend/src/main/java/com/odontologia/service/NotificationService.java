package com.odontologia.service;

import com.odontologia.event.TurnoConfirmadoEvent;

public interface NotificationService {

    String getCanal();

    void enviarConfirmacionTurno(TurnoConfirmadoEvent event);

    String construirMensajeConfirmacion(TurnoConfirmadoEvent event);
}