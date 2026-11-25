package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
import com.odontologia.exception.GlobalExceptionHandler;
import com.odontologia.security.JwtAuthenticationFilter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OdontologoControllerTest.ProbeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, OdontologoControllerTest.ProbeController.class})
class OdontologoControllerTest {

    static final String EJECUTADO = "OPERACION_EJECUTADA";

    /**
     * Controlador de prueba usado para verificar la cadena de seguridad antes de que
     * exista el controller real del change. Los métodos admin llevan el @PreAuthorize
     * previsto para el contrato final.
     */
    @RestController
    @RequestMapping("/api/v1")
    public static class ProbeController {

        @GetMapping("/public/odontologos")
        public List<String> catalogo() {
            return List.of();
        }

        @PostMapping("/admin/odontologos")
        @PreAuthorize("hasRole('ADMIN')")
        public String crear() {
            return EJECUTADO;
        }

        @PutMapping("/admin/odontologos/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String modificar(@PathVariable Long id) {
            return EJECUTADO;
        }

        @DeleteMapping("/admin/odontologos/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String baja(@PathVariable Long id) {
            return EJECUTADO;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
        mockMvc.perform(get("/api/v1/public/odontologos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("POST /api/v1/admin/odontologos con rol PACIENTE debe retornar 403")
    void crear_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(post("/api/v1/admin/odontologos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("PUT /api/v1/admin/odontologos/{id} con rol PACIENTE debe retornar 403")
    void modificar_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(put("/api/v1/admin/odontologos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("DELETE /api/v1/admin/odontologos/{id} con rol PACIENTE debe retornar 403")
    void baja_Paciente_DebeRetornar403() throws Exception {
        verificarAccesoDenegado(delete("/api/v1/admin/odontologos/1"));
    }

    private void verificarAccesoDenegado(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(content().string(not(containsString(EJECUTADO))));
    }
}
