package com.odontologia.repository;

import com.odontologia.model.Odontologo;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OdontologoRepositoryTest {

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Debe persistir un odontólogo vinculado a su usuario y recuperarlo por matrícula, especialidad y estado activo")
    void buscarOdontologo_DebeRecuperarloPorMatriculaEspecialidadYActivo() {
        Usuario usuario = guardarUsuario("odontologo_repo_1", "odontologo_repo_1@clinica.com");
        Odontologo guardado = guardarOdontologo(usuario, "MAT-REPO-001", "Ortodoncia");

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getActivo()).isTrue();
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(guardado.getUsuario().getId()).isEqualTo(usuario.getId());
        assertThat(odontologoRepository.findByMatricula("MAT-REPO-001")).contains(guardado);
        assertThat(odontologoRepository.existsByMatricula("MAT-REPO-001")).isTrue();
        assertThat(odontologoRepository.findByEspecialidad("Ortodoncia")).contains(guardado);
        assertThat(odontologoRepository.findByActivoTrue()).contains(guardado);
    }

    @Test
    @DisplayName("Soft delete debe dejar activo=false en la fila y excluirlo de las consultas derivadas")
    void softDelete_DebeMarcarInactivoYExcluirDeConsultas() {
        Usuario usuario = guardarUsuario("odontologo_repo_2", "odontologo_repo_2@clinica.com");
        Odontologo guardado = guardarOdontologo(usuario, "MAT-REPO-002", "Endodoncia");
        Long id = guardado.getId();

        odontologoRepository.delete(guardado);
        odontologoRepository.flush();
        entityManager.clear();

        Object activoEnFila = entityManager.createNativeQuery("SELECT activo FROM odontologos WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
        assertThat(activoEnFila).isEqualTo(false);
        assertThat(entityManager.createNativeQuery("SELECT COUNT(*) FROM odontologos WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult()).isEqualTo(1L);
        assertThat(odontologoRepository.findById(id)).isEmpty();
        assertThat(odontologoRepository.findByMatricula("MAT-REPO-002")).isEmpty();
        assertThat(odontologoRepository.findByEspecialidad("Endodoncia")).isEmpty();
        assertThat(odontologoRepository.findByActivoTrue())
                .extracting(Odontologo::getId)
                .doesNotContain(id);
    }

    @Test
    @DisplayName("La base de datos debe rechazar matrículas duplicadas")
    void guardarOdontologo_MatriculaDuplicada_DebeLanzarExcepcion() {
        Usuario usuario1 = guardarUsuario("odontologo_repo_3", "odontologo_repo_3@clinica.com");
        Usuario usuario2 = guardarUsuario("odontologo_repo_4", "odontologo_repo_4@clinica.com");
        guardarOdontologo(usuario1, "MAT-DUP-001", "Ortodoncia");

        assertThatThrownBy(() -> guardarOdontologo(usuario2, "MAT-DUP-001", "Endodoncia"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("La base de datos debe rechazar dos odontólogos vinculados al mismo usuario")
    void guardarOdontologo_UsuarioDuplicado_DebeLanzarExcepcion() {
        Usuario usuario = guardarUsuario("odontologo_repo_5", "odontologo_repo_5@clinica.com");
        guardarOdontologo(usuario, "MAT-USR-001", "Ortodoncia");

        assertThatThrownBy(() -> guardarOdontologo(usuario, "MAT-USR-002", "Endodoncia"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("La consulta nativa IncludingInactive debe ver el odontólogo dado de baja")
    void findByMatriculaIncludingInactive_DebeRetornarInactivo() {
        Usuario usuario = guardarUsuario("odontologo_repo_6", "odontologo_repo_6@clinica.com");
        Odontologo guardado = guardarOdontologo(usuario, "MAT-INACT-001", "Endodoncia");

        odontologoRepository.delete(guardado);
        odontologoRepository.flush();
        entityManager.clear();

        assertThat(odontologoRepository.findByMatricula("MAT-INACT-001")).isEmpty();
        assertThat(odontologoRepository.findByMatriculaIncludingInactive("MAT-INACT-001"))
                .isPresent()
                .get()
                .extracting(Odontologo::getActivo)
                .isEqualTo(false);
    }

    private Usuario guardarUsuario(String username, String email) {
        return usuarioRepository.saveAndFlush(Usuario.builder()
                .username(username)
                .email(email)
                .passwordHash("hashedPassword123")
                .rol(Rol.ODONTOLOGO)
                .build());
    }

    private Odontologo guardarOdontologo(Usuario usuario, String matricula, String especialidad) {
        return odontologoRepository.saveAndFlush(Odontologo.builder()
                .nombre("Ana")
                .apellido("Pérez")
                .matricula(matricula)
                .especialidad(especialidad)
                .usuario(usuario)
                .build());
    }
}
