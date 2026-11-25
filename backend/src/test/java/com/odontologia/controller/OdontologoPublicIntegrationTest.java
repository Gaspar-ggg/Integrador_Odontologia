package com.odontologia.controller;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.service.OdontologoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OdontologoPublicIntegrationTest {

    private static final String MATRICULA_PUBLICA = "MAT-INT-PUBLIC-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OdontologoService odontologoService;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("El catálogo sin token debe exponer sólo id, nombre, apellido, matricula y especialidad")
    void catalogo_SinToken_DebeExponerSoloDatosProfesionales() throws Exception {
        odontologoService.crear(request("odontologo_int_public", MATRICULA_PUBLICA));

        String cuerpo = mockMvc.perform(get("/api/v1/public/odontologos"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> profesional = buscarPorMatricula(cuerpo, MATRICULA_PUBLICA);

        assertThat(profesional.keySet())
                .containsExactlyInAnyOrder("id", "nombre", "apellido", "matricula", "especialidad");
        assertThat(((Number) profesional.get("id")).longValue()).isPositive();
        assertThat(profesional.get("nombre")).isEqualTo("Ana");
        assertThat(profesional.get("apellido")).isEqualTo("Pérez");
        assertThat(profesional.get("matricula")).isEqualTo(MATRICULA_PUBLICA);
        assertThat(profesional.get("especialidad")).isEqualTo("Ortodoncia");
    }

    @Test
    @DisplayName("Un profesional dado de baja no debe aparecer en el catálogo público")
    void catalogo_ProfesionalDeBaja_NoDebeAparecer() throws Exception {
        String matricula = "MAT-INT-PUBLIC-002";
        Long id = odontologoService.crear(request("odontologo_int_public_baja", matricula)).getId();

        odontologoService.baja(id);
        entityManager.flush();
        entityManager.clear();

        String cuerpo = mockMvc.perform(get("/api/v1/public/odontologos"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Map<String, Object>> profesionales = objectMapper.readValue(
                cuerpo, new TypeReference<List<Map<String, Object>>>() {});

        assertThat(profesionales).noneMatch(profesional -> matricula.equals(profesional.get("matricula")));
        assertThat(odontologoRepository.findById(id)).isEmpty();
    }

    private Map<String, Object> buscarPorMatricula(String cuerpo, String matricula) throws Exception {
        List<Map<String, Object>> profesionales = objectMapper.readValue(
                cuerpo, new TypeReference<List<Map<String, Object>>>() {});
        return profesionales.stream()
                .filter(profesional -> matricula.equals(profesional.get("matricula")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontró el odontólogo con matrícula " + matricula + " en " + cuerpo));
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
