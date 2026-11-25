package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OdontologoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OdontologoControllerTest {

    private static final String CUERPO_ADMIN = """
            {"nombre":"Ana","apellido":"Pérez","matricula":"MAT-SEG-001",\
            "especialidad":"Ortodoncia","username":"odontologo_seg",\
            "email":"odontologo_seg@clinica.com","password":"password123"}
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
    @DisplayName("GET /api/v1/public/odontologos sin token debe retornar 200")
    void catalogo_SinToken_DebeRetornar200() throws Exception {
        when(odontologoService.catalogoPublico()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/public/odontologos"))
                .andExpect(status().isOk());

        verify(odontologoService).catalogoPublico();
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("POST /api/v1/admin/odontologos con rol PACIENTE debe retornar 403")
    void crear_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(post("/api/v1/admin/odontologos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CUERPO_ADMIN));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("PUT /api/v1/admin/odontologos/{id} con rol PACIENTE debe retornar 403")
    void modificar_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(put("/api/v1/admin/odontologos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CUERPO_ADMIN));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("DELETE /api/v1/admin/odontologos/{id} con rol PACIENTE debe retornar 403")
    void baja_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(delete("/api/v1/admin/odontologos/1"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/v1/admin/odontologos sin credenciales debe retornar 401")
    void crear_SinCredenciales_DebeRetornar401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/odontologos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_ADMIN))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(odontologoService);
    }

    private void verificarAccesoDenegado(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());

        verifyNoInteractions(odontologoService);
    }
}
