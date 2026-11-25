package com.odontologia.service;

import com.odontologia.dto.EvolucionClinicaRequestDTO;
import com.odontologia.dto.EvolucionClinicaResponseDTO;
import com.odontologia.dto.HistoriaClinicaDetalleResponseDTO;
import com.odontologia.exception.BadRequestException;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.*;
import com.odontologia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriaClinicaService {

    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final EvolucionClinicaRepository evolucionClinicaRepository;
    private final OdontologoRepository odontologoRepository;
    private final TurnoRepository turnoRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public HistoriaClinicaDetalleResponseDTO obtenerHistoriaClinicaPorPaciente(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + pacienteId));

        HistoriaClinica historiaClinica = historiaClinicaRepository.findByPacienteId(paciente.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historia clínica no encontrada para el paciente con id: " + pacienteId));

        List<EvolucionClinicaResponseDTO> evoluciones = evolucionClinicaRepository
                .findByHistoriaClinicaIdConDetalles(historiaClinica.getId())
                .stream()
                .map(this::toEvolucionResponseDTO)
                .toList();

        return HistoriaClinicaDetalleResponseDTO.builder()
                .id(historiaClinica.getId())
                .pacienteId(paciente.getId())
                .pacienteNombreCompleto(paciente.getNombre() + " " + paciente.getApellido())
                .pacienteDni(paciente.getDni())
                .fechaCreacion(historiaClinica.getFechaCreacion())
                .observacionesGenerales(historiaClinica.getObservacionesGenerales())
                .evoluciones(evoluciones)
                .build();
    }

    @Transactional
    public EvolucionClinicaResponseDTO registrarEvolucion(Long pacienteId,
                                                          EvolucionClinicaRequestDTO request,
                                                          String usernameOdontologo) {
        Odontologo odontologo = odontologoRepository.findByUsuarioUsername(usernameOdontologo)
                .filter(Odontologo::getActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró un odontólogo activo vinculado al usuario autenticado: " + usernameOdontologo));

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con id: " + pacienteId));

        HistoriaClinica historiaClinica = historiaClinicaRepository.findByPacienteId(paciente.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historia clínica no encontrada para el paciente con id: " + pacienteId));

        Turno turnoAsociado = null;
        if (request.getTurnoId() != null) {
            turnoAsociado = turnoRepository.findById(request.getTurnoId())
                    .filter(Turno::getActivo)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Turno no encontrado con id: " + request.getTurnoId()));

            if (!turnoAsociado.getPaciente().getId().equals(paciente.getId())) {
                throw new BadRequestException("El turno indicado no pertenece al paciente solicitado");
            }

            if (!turnoAsociado.getOdontologo().getId().equals(odontologo.getId())) {
                throw new BadRequestException("El turno indicado está asignado a otro odontólogo");
            }

            if (evolucionClinicaRepository.existsByTurnoId(turnoAsociado.getId())) {
                throw new ConflictException("El turno ya posee una evolución clínica registrada");
            }

            // RN-TU-04: Solo un turno CONFIRMADO puede transicionar a ATENDIDO
            if (!turnoAsociado.getEstado().puedeTransicionarA(EstadoTurno.ATENDIDO)) {
                throw new BadRequestException(
                        "No se puede registrar evolución sobre un turno en estado " + turnoAsociado.getEstado()
                                + ". El turno debe estar CONFIRMADO.");
            }

            turnoAsociado.setEstado(EstadoTurno.ATENDIDO);
            turnoRepository.save(turnoAsociado);
        }

        EvolucionClinica evolucion = EvolucionClinica.builder()
                .historiaClinica(historiaClinica)
                .odontologo(odontologo)
                .turno(turnoAsociado)
                .fechaHora(LocalDateTime.now(clock))
                .diagnostico(request.getDiagnostico().trim())
                .tratamientoRealizado(request.getTratamientoRealizado().trim())
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .build();

        EvolucionClinica guardada = evolucionClinicaRepository.save(evolucion);
        return toEvolucionResponseDTO(guardada);
    }

    private EvolucionClinicaResponseDTO toEvolucionResponseDTO(EvolucionClinica e) {
        Odontologo o = e.getOdontologo();
        return EvolucionClinicaResponseDTO.builder()
                .id(e.getId())
                .historiaClinicaId(e.getHistoriaClinica().getId())
                .pacienteId(e.getHistoriaClinica().getPaciente().getId())
                .odontologoId(o.getId())
                .odontologoNombreCompleto(o.getNombre() + " " + o.getApellido())
                .odontologoMatricula(o.getMatricula())
                .turnoId(e.getTurno() != null ? e.getTurno().getId() : null)
                .fechaHora(e.getFechaHora())
                .diagnostico(e.getDiagnostico())
                .tratamientoRealizado(e.getTratamientoRealizado())
                .observaciones(e.getObservaciones())
                .build();
    }
}