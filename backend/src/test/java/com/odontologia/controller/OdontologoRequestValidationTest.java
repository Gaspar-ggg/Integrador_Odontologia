package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(OdontologoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OdontologoRequestValidationTest {

    private static final String CUERPO_BASE = """
            {"nombre":"Ana","apellido":"Pérez","matricula":"MAT-VAL-001",\
            "especialidad":"Ortodoncia","username":"odontologo_val",\
            "email":"odontologo_val@clinica.com"
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
    @DisplayName("Alta sin password debe retornar 400 con details.password")
    void crear_SinPassword_DebeRetornar400() throws Exception {
        validarInvalido(CUERPO_BASE + "}", "password");
    }

    @Test
    @DisplayName("Alta con password vacío debe retornar 400 con details.password")
    void crear_PasswordVacio_DebeRetornar400() throws Exception {
        validarInvalido(CUERPO_BASE + ",\"password\":\"\"}", "password");
    }

    @Test
    @DisplayName("Alta con password corto debe retornar 400 con details.password")
    void crear_PasswordCorto_DebeRetornar400() throws Exception {
        validarInvalido(CUERPO_BASE + ",\"password\":\"abc\"}", "password");
    }

    @Test
    @DisplayName("Alta sin campos obligatorios debe retornar 400 con details para cada campo")
    void crear_CamposObligatoriosAusentes_DebeRetornar400() throws Exception {
        validarInvalido("{\"password\":\"password123\"}",
                "nombre", "apellido", "matricula", "especialidad", "username", "email");
    }

    @Test
    @DisplayName("ConflictException del alta debe mapearse a 409 con ErrorResponseDTO")
    void crear_Conflicto_DebeRetornar409() throws Exception {
        when(odontologoService.crear(any(OdontologoRequestDTO.class)))
                .thenThrow(new ConflictException("El profesional ya está registrado"));

        mockMvc.perform(post("/api/v1/admin/odontologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_BASE + ",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El profesional ya está registrado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/admin/odontologos"));
    }

    private void validarInvalido(String cuerpo, String... campos) throws Exception {
        var resultado = mockMvc.perform(post("/api/v1/admin/odontologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/admin/odontologos"));

        for (String campo : campos) {
            resultado.andExpect(jsonPath("$.details." + campo).exists());
        }
    }
}
