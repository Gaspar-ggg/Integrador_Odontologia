package com.odontologia.service;

import com.odontologia.event.TurnoConfirmadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class WhatsAppNotificationAdapter implements NotificationService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${notificaciones.whatsapp.mock-enabled:true}")
    private boolean mockEnabled = true;

    @Value("${notificaciones.consultorio.direccion:Av. San Martín 1250, Mendoza}")
    private String direccionConsultorio = "Av. San Martín 1250, Mendoza";

    @Override
    public String getCanal() {
        return "WHATSAPP";
    }

    @Override
    public void enviarConfirmacionTurno(TurnoConfirmadoEvent event) {
        if (event.pacienteTelefono() == null || event.pacienteTelefono().isBlank()) {
            log.warn("[WHATSAPP-SKIP] Turno #{} sin teléfono registrado para {}.",
                    event.turnoId(), event.pacienteNombreCompleto());
            return;
        }

        try {
            String mensaje = construirMensajeConfirmacion(event);
            if (mockEnabled) {
                log.info("[WHATSAPP-MOCK] Turno #{} | Destinatario: {} ({}) | Mensaje: {}",
                        event.turnoId(), event.pacienteNombreCompleto(), event.pacienteTelefono(), mensaje);
                return;
            }
            log.info("[WHATSAPP-SENT] Turno #{} enviado a {}", event.turnoId(), event.pacienteTelefono());
        } catch (Exception ex) {
            log.error("[WHATSAPP-ERROR] Error al notificar Turno #{} por WhatsApp: {}",
                    event.turnoId(), ex.getMessage());
        }
    }

    @Override
    public String construirMensajeConfirmacion(TurnoConfirmadoEvent event) {
        String fecha = event.fechaHoraInicio().toLocalDate().format(FECHA_FMT);
        String hora = event.fechaHoraInicio().toLocalTime().format(HORA_FMT);
        return "Turno confirmado #%d para %s: %s a las %s hs con %s (%s). Consultorio: %s."
                .formatted(
                        event.turnoId(),
                        event.pacienteNombreCompleto(),
                        fecha,
                        hora,
                        event.odontologoNombreCompleto(),
                        event.odontologoEspecialidad(),
                        direccionConsultorio
                );
    }
}