package com.odontologia.repository;

import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Debe persistir un usuario con ID autogenerado, fechas de auditoría y activo true")
    void guardarUsuario_DebePersistirCorrectamente() {
        Usuario usuario = Usuario.builder()
                .username("odontologo1")
                .email("odontologo1@clinica.com")
                .passwordHash("hashedPassword123")
                .rol(Rol.ODONTOLOGO)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getActivo()).isTrue();
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(guardado.getRol()).isEqualTo(Rol.ODONTOLOGO);
    }

    @Test
    @DisplayName("Debe encontrar usuario por username y por email")
    void buscarPorUsernameYEmail_DebeRetornarUsuario() {
        Usuario usuario = Usuario.builder()
                .username("paciente1")
                .email("paciente1@gmail.com")
                .passwordHash("hashedPassword123")
                .rol(Rol.PACIENTE)
                .build();
        usuarioRepository.save(usuario);

        Optional<Usuario> porUsername = usuarioRepository.findByUsername("paciente1");
        Optional<Usuario> porEmail = usuarioRepository.findByEmail("paciente1@gmail.com");

        assertThat(porUsername).isPresent();
        assertThat(porEmail).isPresent();
        assertThat(porUsername.get().getEmail()).isEqualTo("paciente1@gmail.com");
    }

    @Test
    @DisplayName("Debe rechazar la persistencia de username duplicado lanzando DataIntegrityViolationException")
    void guardarUsuario_UsernameDuplicado_DebeLanzarExcepcion() {
        Usuario user1 = Usuario.builder()
                .username("usuario_comun")
                .email("email1@test.com")
                .passwordHash("pass")
                .rol(Rol.PACIENTE)
                .build();
        usuarioRepository.saveAndFlush(user1);

        Usuario user2 = Usuario.builder()
                .username("usuario_comun")
                .email("email2@test.com")
                .passwordHash("pass")
                .rol(Rol.PACIENTE)
                .build();

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Soft delete debe marcar activo=false y excluirlo de consultas estándar findById")
    void softDelete_DebeExcluirDeConsultas() {
        Usuario usuario = Usuario.builder()
                .username("usuario_baja")
                .email("baja@test.com")
                .passwordHash("pass")
                .rol(Rol.RECEPCION)
                .build();
        Usuario guardado = usuarioRepository.saveAndFlush(usuario);
        Long id = guardado.getId();

        usuarioRepository.delete(guardado);
        usuarioRepository.flush();

        Optional<Usuario> encontrado = usuarioRepository.findById(id);
        assertThat(encontrado).isEmpty();
    }

    @Test
    @DisplayName("Consulta nativa debe encontrar usuario inactivo mientras el finder derivado lo excluye por @SQLRestriction")
    void findByUsernameIncludingInactive_DebeRetornarInactivo() {
        Usuario usuario = Usuario.builder()
                .username("inactivo1")
                .email("inactivo1@test.com")
                .passwordHash("hashedPassword123")
                .rol(Rol.PACIENTE)
                .build();
        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        usuarioRepository.delete(guardado);
        usuarioRepository.flush();

        Optional<Usuario> porFinderDerivado = usuarioRepository.findByUsername("inactivo1");
        Optional<Usuario> porNativa = usuarioRepository.findByUsernameIncludingInactive("inactivo1");

        assertThat(porFinderDerivado).isEmpty();
        assertThat(porNativa).isPresent();
        assertThat(porNativa.get().getActivo()).isFalse();
    }
}
