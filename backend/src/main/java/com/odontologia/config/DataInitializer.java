package com.odontologia.config;

import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
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

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsuario("admin", "admin@odontologia.com", "admin123", Rol.ADMIN);
        seedUsuario("recepcion", "recepcion@odontologia.com", "recepcion123", Rol.RECEPCION);
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
}
