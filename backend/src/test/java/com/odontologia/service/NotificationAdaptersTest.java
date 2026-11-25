package com.odontologia.service;

import com.odontologia.event.TurnoConfirmadoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAdaptersTest {

    @Mock
    private JavaMailSender mailSender;

    private TurnoConfirmadoEvent eventoEjemplo(String email, String telefono) {
        return new TurnoConfirmadoEvent(
                42L,
                "Carlos Gómez",
                email,
                telefono,
                "Ana Pérez",
                "Ortodoncia",
                LocalDateTime.of(2026, 12, 15, 10, 30),
                LocalDateTime.of(2026, 12, 15, 11, 0),
                "Control mensual"
        );
    }

    @Test
    void emailAdapter_DatosCompletos_ConstruyeYEnviaCorreoConDatosDelTurno() {
        EmailNotificationAdapter adapter = new EmailNotificationAdapter(mailSender);
        TurnoConfirmadoEvent event = eventoEjemplo("carlos@test.local", "2615551234");

        adapter.enviarConfirmacionTurno(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage enviado = captor.getValue();
        assertArrayEquals(new String[]{"carlos@test.local"}, enviado.getTo());
        assertNotNull(enviado.getText());
        assertTrue(enviado.getText().contains("15/12/2026"));
        assertTrue(enviado.getText().contains("10:30"));
        assertTrue(enviado.getText().contains("Ana Pérez"));
        assertTrue(enviado.getText().contains("Av. San Martín 1250"));
    }

    @Test
    void emailAdapter_FallaSmtp_CapturaExcepcionSinPropagar_RN_NT_02() {
        EmailNotificationAdapter adapter = new EmailNotificationAdapter(mailSender);
        doThrow(new MailSendException("SMTP connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> adapter.enviarConfirmacionTurno(eventoEjemplo("carlos@test.local", "2615551234")));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailAdapter_PacienteSinEmail_OmiteEnvioSinError_SU_02() {
        EmailNotificationAdapter adapter = new EmailNotificationAdapter(mailSender);

        assertDoesNotThrow(() -> adapter.enviarConfirmacionTurno(eventoEjemplo(null, "2615551234")));
        verifyNoInteractions(mailSender);
    }

    @Test
    void whatsAppAdapter_MockHabilitado_GeneraMensajeYEjecutaSinFallas_RN_NT_03() {
        WhatsAppNotificationAdapter adapter = new WhatsAppNotificationAdapter();
        TurnoConfirmadoEvent event = eventoEjemplo("carlos@test.local", "2615551234");

        String mensaje = adapter.construirMensajeConfirmacion(event);
        assertTrue(mensaje.contains("15/12/2026"));
        assertTrue(mensaje.contains("10:30"));
        assertTrue(mensaje.contains("Ana Pérez"));
        assertDoesNotThrow(() -> adapter.enviarConfirmacionTurno(event));
    }
}