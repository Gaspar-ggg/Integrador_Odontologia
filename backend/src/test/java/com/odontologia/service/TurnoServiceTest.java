package com.odontologia.service;

import com.odontologia.dto.DisponibilidadResponseDTO;
import com.odontologia.dto.TurnoRequestDTO;
import com.odontologia.dto.TurnoResponseDTO;
import com.odontologia.exception.BadRequestException;
import com.odontologia.exception.ConflictException;
import com.odontologia.model.EstadoTurno;
import com.odontologia.model.Odontologo;
import com.odontologia.model.Paciente;
import com.odontologia.model.Turno;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.PacienteRepository;
import com.odontologia.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Mendoza");
    private static final Instant INSTANTE_FIJO = LocalDateTime.of(2026, 12, 10, 8, 0).atZone(ZONA).toInstant();

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private OdontologoRepository odontologoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    private TurnoService turnoService;

    private Odontologo odontologoFicticio;
    private Paciente pacienteFicticio;

    @BeforeEach
    void setUp() {
        Clock clockFijo = Clock.fixed(INSTANTE_FIJO, ZONA);
        turnoService = new TurnoService(turnoRepository, odontologoRepository, pacienteRepository, clockFijo);

        odontologoFicticio = Odontologo.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("Pérez")
                .matricula("MP-9901")
                .especialidad("Odontología General")
                .build();

        pacienteFicticio = Paciente.builder()
                .id(10L)
                .dni("99111222")
                .nombre("Carlos")
                .apellido("Gómez")
                .telefono("2610000001")
                .build();
    }

    @Test
    void obtenerDisponibilidad_ExcluyeOcupadosYLiberaCancelados() {
        LocalDate fechaConsulta = LocalDate.of(2026, 12, 11);
        Turno turnoOcupado = Turno.builder()
                .id(100L)
                .odontologo(odontologoFicticio)
                .paciente(pacienteFicticio)
                .fechaHoraInicio(LocalDateTime.of(2026, 12, 11, 10, 0))
                .fechaHoraFin(LocalDateTime.of(2026, 12, 11, 10, 30))
                .estado(EstadoTurno.CONFIRMADO)
                .build();

        when(odontologoRepository.findById(1L)).thenReturn(Optional.of(odontologoFicticio));
        when(turnoRepository.findActivosNoCanceladosEnRango(eq(1L), any(), any()))
                .thenReturn(List.of(turnoOcupado));

        DisponibilidadResponseDTO res = turnoService.obtenerDisponibilidad(1L, fechaConsulta);

        // Jornada 09:00 a 18:00 (18 slots de 30 min) menos 1 ocupado = 17 libres
        assertEquals(17, res.getSlotsDisponibles().size());
        assertFalse(res.getSlotsDisponibles().stream().anyMatch(s -> s.getHoraInicio().equals("10:00")));
        assertTrue(res.getSlotsDisponibles().stream().anyMatch(s -> s.getHoraInicio().equals("10:30")));
    }

    @Test
    void obtenerDisponibilidad_FechaPasada_DebeLanzarBadRequest() {
        LocalDate fechaPasada = LocalDate.of(2026, 12, 9);

        assertThrows(BadRequestException.class,
                () -> turnoService.obtenerDisponibilidad(1L, fechaPasada));
        verifyNoInteractions(turnoRepository);
    }

    @Test
    void crearTurno_Solapado_DebeLanzarConflictException() {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 11, 10, 15);
        LocalDateTime fin = LocalDateTime.of(2026, 12, 11, 10, 45);
        TurnoRequestDTO request = TurnoRequestDTO.builder()
                .odontologoId(1L)
                .pacienteId(10L)
                .fechaHoraInicio(inicio)
                .fechaHoraFin(fin)
                .motivoConsulta("Control general")
                .build();

        when(odontologoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(odontologoFicticio));
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteFicticio));
        when(turnoRepository.existsSolapamiento(1L, inicio, fin)).thenReturn(true);

        assertThrows(ConflictException.class, () -> turnoService.crearTurno(request));
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void crearTurno_ContiguoExacto_DebePermitirReserva() {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 11, 10, 30);
        LocalDateTime fin = LocalDateTime.of(2026, 12, 11, 11, 0);
        TurnoRequestDTO request = TurnoRequestDTO.builder()
                .odontologoId(1L)
                .pacienteId(10L)
                .fechaHoraInicio(inicio)
                .fechaHoraFin(fin)
                .motivoConsulta("Limpieza")
                .build();

        when(odontologoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(odontologoFicticio));
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteFicticio));
        when(turnoRepository.existsSolapamiento(1L, inicio, fin)).thenReturn(false);
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> {
            Turno t = inv.getArgument(0);
            t.setId(55L);
            return t;
        });

        TurnoResponseDTO res = turnoService.crearTurno(request);

        assertEquals(55L, res.getId());
        assertEquals(EstadoTurno.CONFIRMADO, res.getEstado());
    }

    @Test
    void crearTurno_FechaPasada_DebeLanzarBadRequest() {
        TurnoRequestDTO request = TurnoRequestDTO.builder()
                .odontologoId(1L)
                .pacienteId(10L)
                .fechaHoraInicio(LocalDateTime.of(2026, 12, 10, 7, 30))
                .build();

        when(odontologoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(odontologoFicticio));
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteFicticio));

        assertThrows(BadRequestException.class, () -> turnoService.crearTurno(request));
    }

    @Test
    void cancelarTurno_TransicionesPermitidasYTerminales() {
        Turno turnoConfirmado = Turno.builder()
                .id(20L)
                .odontologo(odontologoFicticio)
                .paciente(pacienteFicticio)
                .fechaHoraInicio(LocalDateTime.of(2026, 12, 12, 9, 0))
                .fechaHoraFin(LocalDateTime.of(2026, 12, 12, 9, 30))
                .estado(EstadoTurno.CONFIRMADO)
                .build();

        when(turnoRepository.findById(20L)).thenReturn(Optional.of(turnoConfirmado));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));

        TurnoResponseDTO cancelado = turnoService.cancelarTurno(20L);
        assertEquals(EstadoTurno.CANCELADO, cancelado.getEstado());

        // Intentar cancelar nuevamente un estado terminal CANCELADO falla con 400
        assertThrows(BadRequestException.class, () -> turnoService.cancelarTurno(20L));

        // Intentar cancelar un turno ATENDIDO también falla con 400
        Turno turnoAtendido = Turno.builder()
                .id(21L)
                .odontologo(odontologoFicticio)
                .paciente(pacienteFicticio)
                .estado(EstadoTurno.ATENDIDO)
                .build();
        when(turnoRepository.findById(21L)).thenReturn(Optional.of(turnoAtendido));

        assertThrows(BadRequestException.class, () -> turnoService.cancelarTurno(21L));
    }
}
