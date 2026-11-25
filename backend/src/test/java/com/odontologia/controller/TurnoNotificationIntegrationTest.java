package com.odontologia.controller;

import com.odontologia.model.EstadoTurno;
import com.odontologia.model.Odontologo;
import com.odontologia.model.Paciente;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.PacienteRepository;
import com.odontologia.repository.TurnoRepository;
import com.odontologia.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TurnoNotificationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private OdontologoRepository odontologoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Usuario usuario;
    private Odontologo odontologo;
    private Paciente pacienteConEmail;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
        String sufijo = UUID.randomUUID().toString().substring(0, 6);

        usuario = usuarioRepository.save(Usuario.builder()
                .username("odonto_notif_" + sufijo)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .email("odonto_notif_" + sufijo + "@test.local")
                .rol(Rol.ODONTOLOGO)
                .build());

        odontologo = odontologoRepository.save(Odontologo.builder()
                .nombre("Martín").apellido("Sosa")
                .matricula("MAT-N-" + sufijo).especialidad("Endodoncia-Notif")
                .usuario(usuario).build());

        pacienteConEmail = pacienteRepository.save(Paciente.builder()
                .dni("88" + sufijo).nombre("Lucía").apellido("Méndez")
                .telefono("2614445566").email("lucia_" + sufijo + "@test.local")
                .build());
    }

    @AfterEach
    void tearDown() {
        turnoRepository.deleteAll();
        if (odontologo != null) {
            odontologoRepository.deleteById(odontologo.getId());
        }
        if (pacienteConEmail != null) {
            pacienteRepository.deleteById(pacienteConEmail.getId());
        }
        if (usuario != null) {
            usuarioRepository.deleteById(usuario.getId());
        }
    }

    @Test
    @WithMockUser(roles = "RECEPCION")
    void confirmarTurno_CuandoSmtpFalla_NoHaceRollbackYPersisteConfirmado() throws Exception {
        doThrow(new MailSendException("Servidor SMTP caído"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        LocalDate fecha = LocalDate.now().plusDays(6);
        String payload = """
                {
                  "odontologoId": %d,
                  "pacienteId": %d,
                  "fechaHoraInicio": "%s",
                  "fechaHoraFin": "%s",
                  "motivoConsulta": "Dolor molar"
                }
                """.formatted(odontologo.getId(), pacienteConEmail.getId(),
                fecha.atTime(11, 0), fecha.atTime(11, 30));

        mockMvc.perform(post("/api/v1/turnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        verify(javaMailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));
        assertEquals(1L, turnoRepository.count());
        assertEquals(EstadoTurno.CONFIRMADO, turnoRepository.findAll().get(0).getEstado());
    }
}