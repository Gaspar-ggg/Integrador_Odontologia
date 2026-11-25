package com.odontologia.service;

import com.odontologia.dto.OdontologoPublicDTO;
import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.dto.OdontologoResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.Odontologo;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OdontologoService {

    private final UsuarioRepository usuarioRepository;
    private final OdontologoRepository odontologoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OdontologoResponseDTO crear(OdontologoRequestDTO request) {
        validarDuplicados(request);

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.ODONTOLOGO)
                .build());

        Odontologo odontologo = odontologoRepository.save(Odontologo.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .matricula(request.getMatricula())
                .especialidad(request.getEspecialidad())
                .usuario(usuario)
                .build());

        return toResponse(odontologo, usuario);
    }

    @Transactional
    public OdontologoResponseDTO modificar(Long id, OdontologoRequestDTO request) {
        Odontologo odontologo = odontologoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Odontólogo no encontrado: " + id));

        odontologoRepository.findByMatriculaIncludingInactive(request.getMatricula())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ConflictException("La matrícula ya está registrada");
                });

        odontologo.setNombre(request.getNombre());
        odontologo.setApellido(request.getApellido());
        odontologo.setMatricula(request.getMatricula());
        odontologo.setEspecialidad(request.getEspecialidad());

        Odontologo guardado = odontologoRepository.save(odontologo);
        return toResponse(guardado, guardado.getUsuario());
    }

    @Transactional
    public void baja(Long id) {
        Odontologo odontologo = odontologoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Odontólogo no encontrado: " + id));
        Usuario usuario = odontologo.getUsuario();

        odontologo.desactivar();
        usuario.desactivar();

        odontologoRepository.save(odontologo);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<OdontologoPublicDTO> catalogoPublico() {
        return odontologoRepository.findByActivoTrue().stream()
                .map(this::toPublicDTO)
                .toList();
    }

    private void validarDuplicados(OdontologoRequestDTO request) {
        if (usuarioRepository.findByUsernameIncludingInactive(request.getUsername()).isPresent()
                || usuarioRepository.findByEmailIncludingInactive(request.getEmail()).isPresent()) {
            throw new ConflictException("El username o email ya está registrado");
        }
        if (odontologoRepository.existsByMatricula(request.getMatricula())) {
            throw new ConflictException("La matrícula ya está registrada");
        }
    }

    private OdontologoPublicDTO toPublicDTO(Odontologo odontologo) {
        return OdontologoPublicDTO.builder()
                .id(odontologo.getId())
                .nombre(odontologo.getNombre())
                .apellido(odontologo.getApellido())
                .matricula(odontologo.getMatricula())
                .especialidad(odontologo.getEspecialidad())
                .build();
    }

    private OdontologoResponseDTO toResponse(Odontologo odontologo, Usuario usuario) {
        return OdontologoResponseDTO.builder()
                .id(odontologo.getId())
                .nombre(odontologo.getNombre())
                .apellido(odontologo.getApellido())
                .matricula(odontologo.getMatricula())
                .especialidad(odontologo.getEspecialidad())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .activo(odontologo.getActivo())
                .build();
    }
}
