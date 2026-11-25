package com.odontologia.config;

import com.odontologia.model.Odontologo;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    public static final String ODONTOLOGO_SEED_USERNAME = "odontologo_seed";
    public static final String ODONTOLOGO_SEED_EMAIL = "odontologo.seed@odontologia.com";
    public static final String ODONTOLOGO_SEED_MATRICULA = "MN-SEED-001";

    private static final String ODONTOLOGO_SEED_PASSWORD = "odontologo123";
    private static final String ODONTOLOGO_SEED_NOMBRE = "Ana";
    private static final String ODONTOLOGO_SEED_APELLIDO = "Gómez";
    private static final String ODONTOLOGO_SEED_ESPECIALIDAD = "Ortodoncia";

    private final UsuarioRepository usuarioRepository;
    private final OdontologoRepository odontologoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsuario("admin", "admin@odontologia.com", "admin123", Rol.ADMIN);
        seedUsuario("recepcion", "recepcion@odontologia.com", "recepcion123", Rol.RECEPCION);
        seedOdontologo();
    }

    private void seedUsuario(String username, String email, String rawPassword, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            Usuario usuario = Usuario.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .rol(rol)
                    .build();
            usuarioRepository.save(usuario);
            log.info("Usuario inicial sembrado: {} con rol {}", username, rol);
        }
    }

    private void seedOdontologo() {
        boolean yaExiste = usuarioRepository.existsByUsername(ODONTOLOGO_SEED_USERNAME)
                || odontologoRepository.existsByMatricula(ODONTOLOGO_SEED_MATRICULA)
                || usuarioRepository.findByUsernameIncludingInactive(ODONTOLOGO_SEED_USERNAME).isPresent()
                || usuarioRepository.findByEmailIncludingInactive(ODONTOLOGO_SEED_EMAIL).isPresent()
                || odontologoRepository.findByMatriculaIncludingInactive(ODONTOLOGO_SEED_MATRICULA).isPresent();
        if (yaExiste) {
            return;
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username(ODONTOLOGO_SEED_USERNAME)
                .email(ODONTOLOGO_SEED_EMAIL)
                .passwordHash(passwordEncoder.encode(ODONTOLOGO_SEED_PASSWORD))
                .rol(Rol.ODONTOLOGO)
                .build());
        odontologoRepository.save(Odontologo.builder()
                .nombre(ODONTOLOGO_SEED_NOMBRE)
                .apellido(ODONTOLOGO_SEED_APELLIDO)
                .matricula(ODONTOLOGO_SEED_MATRICULA)
                .especialidad(ODONTOLOGO_SEED_ESPECIALIDAD)
                .usuario(usuario)
                .build());
        log.info("Odontólogo inicial sembrado: {} con matrícula {}", ODONTOLOGO_SEED_USERNAME, ODONTOLOGO_SEED_MATRICULA);
    }
}
