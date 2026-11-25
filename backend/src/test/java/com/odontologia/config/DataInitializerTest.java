package com.odontologia.config;

import com.odontologia.model.Odontologo;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataInitializerTest {

    private static final String SEED_USERNAME = "odontologo_seed";
    private static final String SEED_EMAIL = "odontologo.seed@odontologia.com";
    private static final String SEED_MATRICULA = "MN-SEED-001";
    private static final String SEED_PASSWORD = "odontologo123";

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Ejecutar run() dos veces debe dejar un único profesional de prueba con su única cuenta ODONTOLOGO")
    void run_DosVeces_DebeSerIdempotente() {
        dataInitializer.run();
        dataInitializer.run();

        List<Usuario> cuentasSeed = usuarioRepository.findAll().stream()
                .filter(usuario -> SEED_USERNAME.equals(usuario.getUsername()))
                .toList();

        assertThat(cuentasSeed).hasSize(1);
        Usuario cuenta = cuentasSeed.get(0);
        assertThat(cuenta.getRol()).isEqualTo(Rol.ODONTOLOGO);
        assertThat(cuenta.getEmail()).isEqualTo(SEED_EMAIL);
        assertThat(cuenta.getActivo()).isTrue();
        assertThat(passwordEncoder.matches(SEED_PASSWORD, cuenta.getPasswordHash())).isTrue();

        Object totalFilas = entityManager.createNativeQuery("SELECT COUNT(*) FROM odontologos WHERE matricula = :matricula")
                .setParameter("matricula", SEED_MATRICULA)
                .getSingleResult();
        assertThat(((Number) totalFilas).longValue()).isEqualTo(1L);

        Odontologo profesional = odontologoRepository.findByMatricula(SEED_MATRICULA).orElseThrow();
        assertThat(profesional.getActivo()).isTrue();
        assertThat(profesional.getUsuario().getId()).isEqualTo(cuenta.getId());
    }
}
