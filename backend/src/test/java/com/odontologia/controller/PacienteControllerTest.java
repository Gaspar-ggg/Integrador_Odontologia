package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
import com.odontologia.dto.PacienteRequestDTO;
import com.odontologia.dto.PacienteResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.GlobalExceptionHandler;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.security.JwtAuthenticationFilter;
import com.odontologia.service.PacienteService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacienteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private PacienteService pacienteService;

    private PacienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0, ServletRequest.class), invocation.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));

        responseDTO = PacienteResponseDTO.builder()
                .id(1L)
                .dni("35123456")
                .nombre("María")
                .apellido("Gómez")
                .fechaNacimiento(LocalDate.of(1992, 5, 14))
                .telefono("2615551234")
                .email("maria.gomez@example.com")
                .obraSocial("OSDE")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/pacientes debe retornar 201 para RECEPCION")
    void crearPaciente_Retorna201() throws Exception {
        when(pacienteService.crearPaciente(any(PacienteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/pacientes")
                        .with(user("recepcion").roles("RECEPCION"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dni": "35123456",
                                  "nombre": "María",
                                  "apellido": "Gómez",
                                  "fechaNacimiento": "1992-05-14",
                                  "telefono": "2615551234",
                                  "email": "maria.gomez@example.com",
                                  "obraSocial": "OSDE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.dni").value("35123456"));
    }

    @Test
    @DisplayName("POST /api/v1/pacientes debe retornar 400 cuando faltan campos obligatorios")
    void crearPaciente_Invalido_Retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dni": "", "nombre": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/pacientes debe retornar 409 cuando el DNI ya existe")
    void crearPaciente_DniDuplicado_Retorna409() throws Exception {
        when(pacienteService.crearPaciente(any(PacienteRequestDTO.class)))
                .thenThrow(new ConflictException("Ya existe un paciente registrado con el DNI: 35123456"));

        mockMvc.perform(post("/api/v1/pacientes")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dni": "35123456",
                                  "nombre": "María",
                                  "apellido": "Gómez",
                                  "fechaNacimiento": "1992-05-14",
                                  "telefono": "2615551234"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/pacientes debe retornar 403 para rol PACIENTE")
    void crearPaciente_RolNoAutorizado_Retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .with(user("paciente1").roles("PACIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dni": "35123456",
                                  "nombre": "María",
                                  "apellido": "Gómez",
                                  "fechaNacimiento": "1992-05-14",
                                  "telefono": "2615551234"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/pacientes/buscar debe retornar 200")
    void buscarPacientes_Retorna200() throws Exception {
        when(pacienteService.buscarPacientes("35123456", null)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/pacientes/buscar")
                        .with(user("recepcion").roles("RECEPCION"))
                        .param("dni", "35123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dni").value("35123456"));
    }

    @Test
    @DisplayName("GET /api/v1/pacientes/{id} debe retornar 200")
    void obtenerPorId_Retorna200() throws Exception {
        when(pacienteService.obtenerPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/pacientes/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María"));
    }

    @Test
    @DisplayName("GET /api/v1/pacientes/{id} debe retornar 404 si no existe")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        when(pacienteService.obtenerPorId(99L))
                .thenThrow(new ResourceNotFoundException("Paciente no encontrado con ID: 99"));

        mockMvc.perform(get("/api/v1/pacientes/99")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/pacientes/{id} debe retornar 204")
    void eliminarPaciente_Retorna204() throws Exception {
        doNothing().when(pacienteService).eliminarPaciente(1L);

        mockMvc.perform(delete("/api/v1/pacientes/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}