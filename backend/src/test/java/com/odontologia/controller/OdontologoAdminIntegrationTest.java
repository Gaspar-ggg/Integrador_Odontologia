package com.odontologia.controller;

import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.model.Odontologo;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.UsuarioRepository;
import com.odontologia.service.OdontologoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OdontologoAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OdontologoService odontologoService;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE debe desactivar odontólogo y usuario sin borrar las filas ni listar el profesional")
    void baja_DebeDesactivarProfesionalYCuenta() throws Exception {
        String username = "odontologo_int_baja";
        String matricula = "MAT-INT-BAJA-001";
        Long id = odontologoService.crear(request(username, matricula)).getId();
        Long usuarioId = usuarioRepository.findByUsernameIncludingInactive(username).orElseThrow().getId();

        mockMvc.perform(delete("/api/v1/admin/odontologos/{id}", id))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertThat(activoEn("odontologos", id)).isEqualTo(false);
        assertThat(activoEn("usuarios", usuarioId)).isEqualTo(false);
        assertThat(conteoEn("odontologos", id)).isEqualTo(1L);
        assertThat(conteoEn("usuarios", usuarioId)).isEqualTo(1L);
        assertThat(odontologoRepository.findById(id)).isEmpty();
        assertThat(odontologoRepository.findByActivoTrue())
                .extracting(Odontologo::getId)
                .doesNotContain(id);
        assertThat(usuarioRepository.findByUsername(username)).isEmpty();
    }

    @Test
    @DisplayName("El login con la cuenta dada de baja debe retornar 403 según RN-AU-04")
    void login_TrasBaja_DebeRetornar403() throws Exception {
        String username = "odontologo_int_login_baja";
        Long id = odontologoService.crear(request(username, "MAT-INT-BAJA-002")).getId();

        mockMvc.perform(delete("/api/v1/admin/odontologos/{id}", id)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    private Object activoEn(String tabla, Long id) {
        return entityManager.createNativeQuery("SELECT activo FROM " + tabla + " WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }

    private Object conteoEn(String tabla, Long id) {
        return entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tabla + " WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }

    private OdontologoRequestDTO request(String username, String matricula) {
        return OdontologoRequestDTO.builder()
                .nombre("Ana")
                .apellido("Pérez")
                .matricula(matricula)
                .especialidad("Ortodoncia")
                .username(username)
                .email(username + "@clinica.com")
                .password("password123")
                .build();
    }
}
