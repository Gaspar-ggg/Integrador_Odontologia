package com.odontologia.service;

import com.odontologia.event.TurnoConfirmadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailNotificationAdapter implements NotificationService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final JavaMailSender mailSender;

    @Value("${notificaciones.mail.from:no-reply@clinicaodontologica.local}")
    private String remitente = "no-reply@clinicaodontologica.local";

    @Value("${notificaciones.consultorio.nombre:Clínica Odontológica Integral}")
    private String nombreConsultorio = "Clínica Odontológica Integral";

    @Value("${notificaciones.consultorio.direccion:Av. San Martín 1250, Mendoza}")
    private String direccionConsultorio = "Av. San Martín 1250, Mendoza";

    public EmailNotificationAdapter(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String getCanal() {
        return "EMAIL";
    }

    @Override
    public void enviarConfirmacionTurno(TurnoConfirmadoEvent event) {
        if (event.pacienteEmail() == null || event.pacienteEmail().isBlank()) {
            log.warn("[EMAIL-SKIP] Turno #{} confirmado para {} sin dirección de correo registrada (SU-02).",
                    event.turnoId(), event.pacienteNombreCompleto());
            return;
        }
        if (mailSender == null) {
            log.warn("[EMAIL-SKIP] JavaMailSender no disponible en este contexto para Turno #{}.", event.turnoId());
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(remitente);
            mail.setTo(event.pacienteEmail());
            mail.setSubject("Confirmación de Turno #" + event.turnoId() + " - " + nombreConsultorio);
            mail.setText(construirMensajeConfirmacion(event));

            mailSender.send(mail);
            log.info("[EMAIL-SENT] Confirmación de Turno #{} enviada a {}", event.turnoId(), event.pacienteEmail());
        } catch (Exception ex) {
            log.error("[EMAIL-ERROR] Falló el envío de correo para Turno #{} a {}: {}",
                    event.turnoId(), event.pacienteEmail(), ex.getMessage());
        }
    }

    @Override
    public String construirMensajeConfirmacion(TurnoConfirmadoEvent event) {
        String fecha = event.fechaHoraInicio().toLocalDate().format(FECHA_FMT);
        String hora = event.fechaHoraInicio().toLocalTime().format(HORA_FMT);
        return """
                Hola %s,

                Tu turno en %s ha sido confirmado con el siguiente detalle:
                - Fecha: %s
                - Hora: %s hs
                - Profesional: %s (%s)
                - Dirección del consultorio: %s

                Ante cualquier inconveniente, comunicate con recepción para reprogramar o cancelar.
                """.formatted(
                event.pacienteNombreCompleto(),
                nombreConsultorio,
                fecha,
                hora,
                event.odontologoNombreCompleto(),
                event.odontologoEspecialidad(),
                direccionConsultorio
        );
    }
}