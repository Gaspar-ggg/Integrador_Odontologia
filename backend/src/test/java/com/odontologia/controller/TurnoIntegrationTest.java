package com.odontologia.controller;

import com.odontologia.dto.TurnoRequestDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.model.*;
import com.odontologia.repository.*;
import com.odontologia.service.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TurnoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TurnoService turnoService;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private OdontologoRepository odontologoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Odontologo odontologo;
    private Paciente paciente1;
    private Paciente paciente2;

    @BeforeEach
    void setUp() {
        turnoRepository.deleteAll();
        String sufijo = UUID.randomUUID().toString().substring(0, 6);
        Usuario userOdonto = usuarioRepository.save(Usuario.builder()
                .username("odonto_test_" + sufijo)
                .passwordHash("$2a$10$abcdefghijklmnopqrstuv")
                .email("odonto_" + sufijo + "@test.local")
                .rol(Rol.ODONTOLOGO)
                .build());

        odontologo = odontologoRepository.save(Odontologo.builder()
                .nombre("Laura").apellido("Ficticia")
                .matricula("MAT-" + sufijo).especialidad("Ortodoncia")
                .usuario(userOdonto).build());

        paciente1 = pacienteRepository.save(Paciente.builder()
                .dni("90" + sufijo).nombre("Juan").apellido("PruebaUno")
                .telefono("2611111111").build());

        paciente2 = pacienteRepository.save(Paciente.builder()
                .dni("91" + sufijo).nombre("María").apellido("PruebaDos")
                .telefono("2612222222").build());
    }
    @Test
    @WithMockUser(roles = "RECEPCION")
    void flujoCompleto_ReservarColisionCancelarYVolverAReservar() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(5);
        String body1 = """
                {"odontologoId":%d,"pacienteId":%d,"fechaHoraInicio":"%s","fechaHoraFin":"%s","motivoConsulta":"Control"}
                """.formatted(odontologo.getId(), paciente1.getId(), fecha.atTime(10, 0), fecha.atTime(10, 30));

        String json = mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(body1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andReturn().getResponse().getContentAsString();

        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        m.find();
        Long turnoId = Long.parseLong(m.group(1));

        String bodySolapado = """
                {"odontologoId":%d,"pacienteId":%d,"fechaHoraInicio":"%s","fechaHoraFin":"%s","motivoConsulta":"Choque"}
                """.formatted(odontologo.getId(), paciente2.getId(), fecha.atTime(10, 15), fecha.atTime(10, 45));

        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(bodySolapado))
                .andExpect(status().isConflict());

        String bodyContiguo = """
                {"odontologoId":%d,"pacienteId":%d,"fechaHoraInicio":"%s","fechaHoraFin":"%s","motivoConsulta":"Contiguo"}
                """.formatted(odontologo.getId(), paciente2.getId(), fecha.atTime(10, 30), fecha.atTime(11, 0));

        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(bodyContiguo))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/turnos/" + turnoId + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(put("/api/v1/turnos/" + turnoId + "/cancelar"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/turnos").contentType(MediaType.APPLICATION_JSON).content(body1))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/odontologos/" + odontologo.getId() + "/disponibilidad").param("fecha", fecha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.odontologoId").value(odontologo.getId()));

        mockMvc.perform(get("/api/v1/turnos").param("odontologoId", odontologo.getId().toString())
                        .param("fecha", fecha.toString()).param("estado", EstadoTurno.CONFIRMADO.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void concurrencia_DosReservasSimultaneasMismoSlot_UnaExitoYUnaConflict() throws Exception {
        LocalDateTime inicio = LocalDate.now().plusDays(7).atTime(15, 0);
        LocalDateTime fin = inicio.plusMinutes(30);
        TurnoRequestDTO reqA = TurnoRequestDTO.builder().odontologoId(odontologo.getId()).pacienteId(paciente1.getId())
                .fechaHoraInicio(inicio).fechaHoraFin(fin).motivoConsulta("Hilo A").build();
        TurnoRequestDTO reqB = TurnoRequestDTO.builder().odontologoId(odontologo.getId()).pacienteId(paciente2.getId())
                .fechaHoraInicio(inicio).fechaHoraFin(fin).motivoConsulta("Hilo B").build();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch listo = new CountDownLatch(2);
        CountDownLatch arrancar = new CountDownLatch(1);
        List<String> res = Collections.synchronizedList(new ArrayList<>());

        Callable<Void> tareaA = () -> { listo.countDown(); arrancar.await();
            try { turnoService.crearTurno(reqA); res.add("OK"); } catch (ConflictException e) { res.add("CONFLICT"); } return null; };
        Callable<Void> tareaB = () -> { listo.countDown(); arrancar.await();
            try { turnoService.crearTurno(reqB); res.add("OK"); } catch (ConflictException e) { res.add("CONFLICT"); } return null; };

        Future<Void> f1 = pool.submit(tareaA);
        Future<Void> f2 = pool.submit(tareaB);
        listo.await(); arrancar.countDown(); f1.get(); f2.get(); pool.shutdown();

        assertEquals(1, res.stream().filter("OK"::equals).count());
        assertEquals(1, res.stream().filter("CONFLICT"::equals).count());
    }
}