package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.dto.OdontologoResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.GlobalExceptionHandler;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.security.JwtAuthenticationFilter;
import com.odontologia.service.OdontologoService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(OdontologoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OdontologoAdminControllerTest {

    private static final String CUERPO_ALTA = """
            {"nombre":"Ana","apellido":"Pérez","matricula":"MAT-CTRL-001",\
            "especialidad":"Ortodoncia","username":"odontologo_ctrl",\
            "email":"odontologo_ctrl@clinica.com","password":"password123"}
            """;

    private static final String CUERPO_MODIFICAR = """
            {"nombre":"Ana María","apellido":"Pérez","matricula":"MAT-CTRL-002",\
            "especialidad":"Endodoncia","username":"odontologo_ctrl",\
            "email":"odontologo_ctrl@clinica.com","password":"password123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OdontologoService odontologoService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0, ServletRequest.class), invocation.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    @DisplayName("POST /api/v1/admin/odontologos con ADMIN debe retornar 201 sin password")
    void crear_Admin_DebeRetornar201SinPassword() throws Exception {
        when(odontologoService.crear(any(OdontologoRequestDTO.class)))
                .thenReturn(OdontologoResponseDTO.builder()
                        .id(11L)
                        .nombre("Ana")
                        .apellido("Pérez")
                        .matricula("MAT-CTRL-001")
                        .especialidad("Ortodoncia")
                        .username("odontologo_ctrl")
                        .email("odontologo_ctrl@clinica.com")
                        .activo(true)
                        .build());

        mockMvc.perform(post("/api/v1/admin/odontologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ALTA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.apellido").value("Pérez"))
                .andExpect(jsonPath("$.matricula").value("MAT-CTRL-001"))
                .andExpect(jsonPath("$.especialidad").value("Ortodoncia"))
                .andExpect(jsonPath("$.username").value("odontologo_ctrl"))
                .andExpect(jsonPath("$.email").value("odontologo_ctrl@clinica.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(odontologoService).crear(any(OdontologoRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("POST /api/v1/admin/odontologos con PACIENTE debe retornar 403 y no ejecutar el alta")
    void crear_Paciente_DebeRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/odontologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ALTA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));

        verifyNoInteractions(odontologoService);
    }

    @Test
    @DisplayName("PUT /api/v1/admin/odontologos/{id} con ADMIN debe retornar 200 y conservar la cuenta vinculada")
    void modificar_Admin_DebeRetornar200YConservarCuenta() throws Exception {
        when(odontologoService.modificar(eq(11L), any(OdontologoRequestDTO.class)))
                .thenReturn(OdontologoResponseDTO.builder()
                        .id(11L)
                        .nombre("Ana María")
                        .apellido("Pérez")
                        .matricula("MAT-CTRL-002")
                        .especialidad("Endodoncia")
                        .username("odontologo_ctrl")
                        .email("odontologo_ctrl@clinica.com")
                        .activo(true)
                        .build());

        mockMvc.perform(put("/api/v1/admin/odontologos/{id}", 11)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MODIFICAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.nombre").value("Ana María"))
                .andExpect(jsonPath("$.matricula").value("MAT-CTRL-002"))
                .andExpect(jsonPath("$.especialidad").value("Endodoncia"))
                .andExpect(jsonPath("$.username").value("odontologo_ctrl"))
                .andExpect(jsonPath("$.email").value("odontologo_ctrl@clinica.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(odontologoService).modificar(eq(11L), any(OdontologoRequestDTO.class));
    }

    @Test
    @DisplayName("PUT con id inexistente debe retornar 404 con ErrorResponseDTO")
    void modificar_Inexistente_DebeRetornar404() throws Exception {
        when(odontologoService.modificar(eq(99L), any(OdontologoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Odontólogo no encontrado: 99"));

        mockMvc.perform(put("/api/v1/admin/odontologos/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MODIFICAR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Odontólogo no encontrado: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/odontologos/99"));
    }

    @Test
    @DisplayName("PUT con matrícula ajena debe retornar 409 con ErrorResponseDTO")
    void modificar_MatriculaAjena_DebeRetornar409() throws Exception {
        when(odontologoService.modificar(eq(11L), any(OdontologoRequestDTO.class)))
                .thenThrow(new ConflictException("La matrícula ya está registrada"));

        mockMvc.perform(put("/api/v1/admin/odontologos/{id}", 11)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MODIFICAR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("La matrícula ya está registrada"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("PUT con rol PACIENTE debe retornar 403 y no ejecutar la modificación")
    void modificar_Paciente_DebeRetornar403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/odontologos/{id}", 11)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MODIFICAR))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(odontologoService);
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/odontologos/{id} con ADMIN debe retornar 204")
    void baja_Admin_DebeRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/odontologos/{id}", 11))
                .andExpect(status().isNoContent());

        verify(odontologoService).baja(11L);
    }

    @Test
    @DisplayName("DELETE con id inexistente debe retornar 404 con ErrorResponseDTO")
    void baja_Inexistente_DebeRetornar404() throws Exception {
        doThrow(new ResourceNotFoundException("Odontólogo no encontrado: 99"))
                .when(odontologoService).baja(99L);

        mockMvc.perform(delete("/api/v1/admin/odontologos/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Odontólogo no encontrado: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/odontologos/99"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("DELETE con rol PACIENTE debe retornar 403 y no ejecutar la baja")
    void baja_Paciente_DebeRetornar403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/odontologos/{id}", 11))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(odontologoService);
    }
}
