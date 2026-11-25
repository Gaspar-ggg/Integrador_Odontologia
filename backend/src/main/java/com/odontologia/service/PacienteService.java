package com.odontologia.service;

import com.odontologia.dto.PacienteRequestDTO;
import com.odontologia.dto.PacienteResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.HistoriaClinica;
import com.odontologia.model.Paciente;
import com.odontologia.model.Usuario;
import com.odontologia.repository.HistoriaClinicaRepository;
import com.odontologia.repository.PacienteRepository;
import com.odontologia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PacienteResponseDTO crearPaciente(PacienteRequestDTO request) {
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new ConflictException("Ya existe un paciente registrado con el DNI: " + request.getDni());
        }

        Usuario usuario = null;
        if (request.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.getUsuarioId()));
        }

        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .fechaNacimiento(request.getFechaNacimiento())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .obraSocial(request.getObraSocial())
                .build();

        Paciente guardado = pacienteRepository.save(paciente);

        // RN-PA-02: Creación automática de Historia Clínica
        HistoriaClinica hc = HistoriaClinica.builder()
                .paciente(guardado)
                .build();
        historiaClinicaRepository.save(hc);

        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscarPacientes(String dni, String q) {
        return pacienteRepository.buscarAgil(dni, q).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));
        return mapToResponse(paciente);
    }

    @Transactional
    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }
        pacienteRepository.deleteById(id);
    }

    private PacienteResponseDTO mapToResponse(Paciente p) {
        return PacienteResponseDTO.builder()
                .id(p.getId())
                .usuarioId(p.getUsuario() != null ? p.getUsuario().getId() : null)
                .dni(p.getDni())
                .nombre(p.getNombre())
                .apellido(p.getApellido())
                .fechaNacimiento(p.getFechaNacimiento())
                .telefono(p.getTelefono())
                .email(p.getEmail())
                .obraSocial(p.getObraSocial())
                .activo(p.getActivo())
                .build();
    }
}
