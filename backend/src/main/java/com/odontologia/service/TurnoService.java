package com.odontologia.service;

import com.odontologia.dto.DisponibilidadResponseDTO;
import com.odontologia.dto.SlotHorarioDTO;
import com.odontologia.dto.TurnoRequestDTO;
import com.odontologia.dto.TurnoResponseDTO;
import com.odontologia.exception.BadRequestException;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.EstadoTurno;
import com.odontologia.model.Odontologo;
import com.odontologia.model.Paciente;
import com.odontologia.model.Turno;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.PacienteRepository;
import com.odontologia.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private static final Set<Long> DURACIONES_ESTANDAR_MINUTOS = Set.of(30L, 45L);
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoRepository turnoRepository;
    private final OdontologoRepository odontologoRepository;
    private final PacienteRepository pacienteRepository;
    private final Clock clock;

    @Value("${agenda.duracion-slot-minutos:30}")
    private int duracionSlotMinutos = 30;

    @Value("${agenda.hora-inicio:09:00}")
    private String horaInicioJornada = "09:00";

    @Value("${agenda.hora-fin:18:00}")
    private String horaFinJornada = "18:00";

    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO obtenerDisponibilidad(Long odontologoId, LocalDate fecha) {
        if (fecha == null) {
            throw new BadRequestException("La fecha de consulta es obligatoria");
        }

        LocalDate hoy = LocalDate.now(clock);
        if (fecha.isBefore(hoy)) {
            throw new BadRequestException("No se pueden consultar ni agendar turnos en fechas pasadas");
        }

        Odontologo odontologo = odontologoRepository.findById(odontologoId)
                .orElseThrow(() -> new ResourceNotFoundException("Odontólogo no encontrado con id: " + odontologoId));

        LocalTime inicioJornada = LocalTime.parse(horaInicioJornada);
        LocalTime finJornada = LocalTime.parse(horaFinJornada);
        LocalDateTime inicioDia = fecha.atTime(inicioJornada);
        LocalDateTime finDia = fecha.atTime(finJornada);
        LocalDateTime ahora = LocalDateTime.now(clock);

        List<Turno> turnosOcupados = turnoRepository.findActivosNoCanceladosEnRango(
                odontologoId,
                fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay()
        );

        List<SlotHorarioDTO> slotsLibres = new ArrayList<>();
        LocalDateTime cursor = inicioDia;

        while (!cursor.plusMinutes(duracionSlotMinutos).isAfter(finDia)) {
            LocalDateTime slotInicio = cursor;
            LocalDateTime slotFin = cursor.plusMinutes(duracionSlotMinutos);

            boolean esFuturo = slotInicio.isAfter(ahora);
            boolean estaLibre = turnosOcupados.stream()
                    .noneMatch(t -> seSolapan(slotInicio, slotFin, t.getFechaHoraInicio(), t.getFechaHoraFin()));

            if (esFuturo && estaLibre) {
                slotsLibres.add(SlotHorarioDTO.builder()
                        .fechaHoraInicio(slotInicio)
                        .fechaHoraFin(slotFin)
                        .horaInicio(slotInicio.toLocalTime().format(HORA_FORMATTER))
                        .horaFin(slotFin.toLocalTime().format(HORA_FORMATTER))
                        .build());
            }
            cursor = slotFin;
        }

        return DisponibilidadResponseDTO.builder()
                .odontologoId(odontologo.getId())
                .odontologoNombreCompleto(odontologo.getNombre() + " " + odontologo.getApellido())
                .fecha(fecha)
                .duracionMinutos(duracionSlotMinutos)
                .slotsDisponibles(slotsLibres)
                .build();
    }

    @Transactional
    public TurnoResponseDTO crearTurno(TurnoRequestDTO request) {
        Odontologo odontologo = odontologoRepository.findByIdForUpdate(request.getOdontologoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Odontólogo no encontrado con id: " + request.getOdontologoId()));

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente no encontrado con id: " + request.getPacienteId()));

        LocalDateTime inicio = request.getFechaHoraInicio();
        LocalDateTime fin = request.getFechaHoraFin() != null
                ? request.getFechaHoraFin()
                : inicio.plusMinutes(duracionSlotMinutos);

        validarHorarioTurno(inicio, fin);

        if (turnoRepository.existsSolapamiento(odontologo.getId(), inicio, fin)) {
            throw new ConflictException("El turno seleccionado ya no se encuentra disponible");
        }

        EstadoTurno estadoInicial = resolverEstadoInicial(request.getEstado());

        Turno turno = Turno.builder()
                .paciente(paciente)
                .odontologo(odontologo)
                .fechaHoraInicio(inicio)
                .fechaHoraFin(fin)
                .estado(estadoInicial)
                .motivoConsulta(request.getMotivoConsulta())
                .build();

        Turno guardado = turnoRepository.save(turno);
        return toResponseDTO(guardado);
    }

    @Transactional
    public TurnoResponseDTO cancelarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + turnoId));

        if (!turno.getEstado().puedeTransicionarA(EstadoTurno.CANCELADO)) {
            throw new BadRequestException(
                    "No es posible cancelar un turno en estado " + turno.getEstado());
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        Turno actualizado = turnoRepository.save(turno);
        return toResponseDTO(actualizado);
    }

    @Transactional
    public TurnoResponseDTO confirmarTurno(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + turnoId));

        if (!turno.getEstado().puedeTransicionarA(EstadoTurno.CONFIRMADO)) {
            throw new BadRequestException(
                    "No es posible confirmar un turno en estado " + turno.getEstado());
        }

        turno.setEstado(EstadoTurno.CONFIRMADO);
        Turno actualizado = turnoRepository.save(turno);
        return toResponseDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> listarTurnos(Long odontologoId,
                                               Long pacienteId,
                                               LocalDate fecha,
                                               EstadoTurno estado) {
        LocalDateTime desde = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime hasta = fecha != null ? fecha.plusDays(1).atStartOfDay() : null;

        return turnoRepository.findByFiltros(odontologoId, pacienteId, estado, desde, hasta)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private void validarHorarioTurno(LocalDateTime inicio, LocalDateTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new BadRequestException("La fecha y hora de fin debe ser posterior a la de inicio");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        if (!inicio.isAfter(ahora)) {
            throw new BadRequestException("No se pueden agendar turnos en fechas u horas pasadas");
        }

        long minutos = Duration.between(inicio, fin).toMinutes();
        if (!DURACIONES_ESTANDAR_MINUTOS.contains(minutos)) {
            throw new BadRequestException("La duración del turno debe ser un bloque fijo estándar de 30 o 45 minutos");
        }

        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new BadRequestException("Un turno no puede extenderse entre dos días distintos");
        }

        LocalTime inicioJornada = LocalTime.parse(horaInicioJornada);
        LocalTime finJornada = LocalTime.parse(horaFinJornada);
        if (inicio.toLocalTime().isBefore(inicioJornada) || fin.toLocalTime().isAfter(finJornada)) {
            throw new BadRequestException(
                    "El turno debe encontrarse dentro del horario de atención (" + horaInicioJornada + " a " + horaFinJornada + ")");
        }
    }

    private EstadoTurno resolverEstadoInicial(EstadoTurno solicitado) {
        if (solicitado == null) {
            return EstadoTurno.CONFIRMADO;
        }
        if (solicitado != EstadoTurno.PENDIENTE && solicitado != EstadoTurno.CONFIRMADO) {
            throw new BadRequestException("Un turno nuevo solo puede crearse en estado PENDIENTE o CONFIRMADO");
        }
        return solicitado;
    }

    private boolean seSolapan(LocalDateTime inicioA, LocalDateTime finA, LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    private TurnoResponseDTO toResponseDTO(Turno turno) {
        Paciente p = turno.getPaciente();
        Odontologo o = turno.getOdontologo();
        return TurnoResponseDTO.builder()
                .id(turno.getId())
                .pacienteId(p.getId())
                .pacienteNombreCompleto(p.getNombre() + " " + p.getApellido())
                .odontologoId(o.getId())
                .odontologoNombreCompleto(o.getNombre() + " " + o.getApellido())
                .odontologoEspecialidad(o.getEspecialidad())
                .fechaHoraInicio(turno.getFechaHoraInicio())
                .fechaHoraFin(turno.getFechaHoraFin())
                .estado(turno.getEstado())
                .motivoConsulta(turno.getMotivoConsulta())
                .activo(turno.getActivo())
                .build();
    }
}