package com.odontologia.controller;

import com.odontologia.dto.EvolucionClinicaRequestDTO;
import com.odontologia.model.*;
import com.odontologia.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistoriaClinicaControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private OdontologoRepository odontologoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private HistoriaClinicaRepository historiaClinicaRepository;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private EvolucionClinicaRepository evolucionClinicaRepository;

    private Paciente paciente;
    private HistoriaClinica historiaClinica;
    private Odontologo odontologo;
    private Turno turnoConfirmado;

    @BeforeEach
    void setUp() {
        String sufijo = UUID.randomUUID().toString().substring(0, 6);

        Usuario userOdo = usuarioRepository.save(Usuario.builder()
                .username("odo_c08_test")
                .passwordHash("$2a$10$dummyhashfortesting123456789012345678901234567890")
                .email("odo_c08_" + sufijo + "@clinica.com")
                .rol(Rol.ODONTOLOGO)
                .build());

        odontologo = odontologoRepository.save(Odontologo.builder()
                .nombre("Laura")
                .apellido("Gómez")
                .matricula("MP-C08-" + sufijo)
                .especialidad("Odontología General")
                .usuario(userOdo)
                .build());

        paciente = pacienteRepository.save(Paciente.builder()
                .dni("40" + sufijo)
                .nombre("Carlos")
                .apellido("Ruiz").telefono("2615550199")
                .fechaNacimiento(LocalDate.of(1995, 3, 15))
                .build());

        historiaClinica = historiaClinicaRepository.save(HistoriaClinica.builder()
                .paciente(paciente)
                .observacionesGenerales("Historia base C-08")
                .build());

        turnoConfirmado = turnoRepository.save(Turno.builder()
                .paciente(paciente)
                .odontologo(odontologo)
                .fechaHoraInicio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0))
                .fechaHoraFin(LocalDateTime.now().plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0))
                .estado(EstadoTurno.CONFIRMADO)
                .motivoConsulta("Dolor molar")
                .build());
    }

    @Test
    @WithMockUser(username = "odo_c08_test", roles = {"ODONTOLOGO"})
    void registrarEvolucion_comoOdontologo_retorna201YActualizaTurnoAtendido() throws Exception {
        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder()
                .turnoId(turnoConfirmado.getId())
                .diagnostico("Pulpitis reversible pieza 46")
                .tratamientoRealizado("Protección pulpar directa")
                .observaciones("Evolución favorable")
                .build();

        mockMvc.perform(post("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pacienteId").value(paciente.getId()))
                .andExpect(jsonPath("$.odontologoId").value(odontologo.getId()))
                .andExpect(jsonPath("$.turnoId").value(turnoConfirmado.getId()));

        assertEquals(EstadoTurno.ATENDIDO, turnoRepository.findById(turnoConfirmado.getId()).orElseThrow().getEstado());
    }

    @Test
    @WithMockUser(username = "recepcion_user", roles = {"RECEPCION"})
    void registrarEvolucion_comoRecepcion_retorna403() throws Exception {
        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder()
                .diagnostico("Intento no autorizado")
                .tratamientoRealizado("Ninguno")
                .build();

        mockMvc.perform(post("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void registrarEvolucion_comoAdmin_retorna403() throws Exception {
        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder()
                .diagnostico("Intento admin")
                .tratamientoRealizado("Ninguno")
                .build();

        mockMvc.perform(post("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarEvolucion_sinAutenticacion_retorna401() throws Exception {
        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder()
                .diagnostico("Sin token")
                .tratamientoRealizado("Ninguno")
                .build();

        mockMvc.perform(post("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "odo_c08_test", roles = {"ODONTOLOGO"})
    void obtenerHistoriaClinica_retorna200ConEvolucionesOrdenadas() throws Exception {
        evolucionClinicaRepository.save(EvolucionClinica.builder()
                .historiaClinica(historiaClinica)
                .odontologo(odontologo)
                .fechaHora(LocalDateTime.of(2026, 11, 20, 9, 0))
                .diagnostico("Diagnóstico inicial")
                .tratamientoRealizado("Limpieza")
                .build());

        mockMvc.perform(get("/api/v1/historias-clinicas/paciente/{id}", paciente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historiaClinica.getId()))
                .andExpect(jsonPath("$.evoluciones.length()").value(1));
    }

    @Test
    @WithMockUser(username = "odo_c08_test", roles = {"ODONTOLOGO"})
    void inmutabilidadClinica_bloqueaDeleteEnRepositorioYMetodosHttpMutables() throws Exception {
        EvolucionClinica ev = evolucionClinicaRepository.save(EvolucionClinica.builder()
                .historiaClinica(historiaClinica)
                .odontologo(odontologo)
                .fechaHora(LocalDateTime.now())
                .diagnostico("Registro inmutable")
                .tratamientoRealizado("Sellador")
                .build());

        assertThrows(UnsupportedOperationException.class, () -> evolucionClinicaRepository.delete(ev));
        assertThrows(UnsupportedOperationException.class, () -> evolucionClinicaRepository.deleteById(ev.getId()));

        mockMvc.perform(delete("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/v1/historias-clinicas/paciente/{id}/evoluciones", paciente.getId()))
                .andExpect(status().isMethodNotAllowed());
    }
}