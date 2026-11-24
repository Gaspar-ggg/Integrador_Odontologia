package com.odontologia.service;

import com.odontologia.dto.AuthResponse;
import com.odontologia.dto.LoginRequest;
import com.odontologia.dto.RegisterRequest;
import com.odontologia.dto.UserMeResponse;
import com.odontologia.exception.AccountDisabledException;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.UsuarioRepository;
import com.odontologia.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsernameIncludingInactive(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new AccountDisabledException("Cuenta deshabilitada");
        }

        String token = jwtTokenProvider.generateToken(
                usuario.getUsername(),
                usuario.getId(),
                usuario.getRol().name()
        );

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .rol(usuario.getRol().name())
                .build();
    }

    @Transactional
    public UserMeResponse register(RegisterRequest request) {
        if (usuarioRepository.findByUsernameIncludingInactive(request.getUsername()).isPresent()
                || usuarioRepository.findByEmailIncludingInactive(request.getEmail()).isPresent()) {
            throw new ConflictException("El username o email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.PACIENTE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return toUserMeResponse(guardado);
    }

    @Transactional(readOnly = true)
    public UserMeResponse getCurrentUser(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return toUserMeResponse(usuario);
    }

    private UserMeResponse toUserMeResponse(Usuario usuario) {
        return UserMeResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .activo(usuario.getActivo())
                .build();
    }
}
