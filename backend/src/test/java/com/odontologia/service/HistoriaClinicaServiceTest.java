package com.odontologia.service;

import com.odontologia.dto.EvolucionClinicaRequestDTO;
import com.odontologia.dto.EvolucionClinicaResponseDTO;
import com.odontologia.dto.HistoriaClinicaDetalleResponseDTO;
import com.odontologia.exception.BadRequestException;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.*;
import com.odontologia.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private HistoriaClinicaRepository historiaClinicaRepository;
    @Mock private EvolucionClinicaRepository evolucionClinicaRepository;
    @Mock private OdontologoRepository odontologoRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private Clock clock;

    @InjectMocks
    private HistoriaClinicaService historiaClinicaService;

    private Odontologo odontologo;
    private Paciente paciente;
    private HistoriaClinica historiaClinica;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-11-25T14:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        Usuario usuarioOdo = Usuario.builder().id(10L).username("dr.house").email("house@clinica.com").rol(Rol.ODONTOLOGO).build();
        usuarioOdo.setActivo(true);

        odontologo = Odontologo.builder().id(1L).nombre("Gregory").apellido("House").matricula("MP-9999").especialidad("Endodoncia").usuario(usuarioOdo).build();
        odontologo.setActivo(true);

        paciente = Paciente.builder().id(5L).dni("35111222").nombre("Juan").apellido("Pérez").fechaNacimiento(LocalDate.of(1990, 5, 10)).build();
        paciente.setActivo(true);

        historiaClinica = HistoriaClinica.builder().id(50L).paciente(paciente).fechaCreacion(LocalDateTime.of(2026, 11, 1, 10, 0)).observacionesGenerales("Sin alergias").build();
    }

    @Test
    void registrarEvolucion_conTurnoConfirmado_creaEvolucionYMarcaTurnoAtendido() {
        Turno turno = Turno.builder().id(100L).paciente(paciente).odontologo(odontologo).estado(EstadoTurno.CONFIRMADO).build();
        turno.setActivo(true);

        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder()
                .turnoId(100L).diagnostico("Caries profunda pieza 36").tratamientoRealizado("Obturación con resina").observaciones("Control en 6 meses").build();

        when(odontologoRepository.findByUsuarioUsername("dr.house")).thenReturn(Optional.of(odontologo));
        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(turnoRepository.findById(100L)).thenReturn(Optional.of(turno));
        when(evolucionClinicaRepository.existsByTurnoId(100L)).thenReturn(false);
        when(evolucionClinicaRepository.save(any(EvolucionClinica.class))).thenAnswer(inv -> {
            EvolucionClinica e = inv.getArgument(0);
            return EvolucionClinica.builder().id(200L).historiaClinica(e.getHistoriaClinica()).odontologo(e.getOdontologo()).turno(e.getTurno())
                    .fechaHora(e.getFechaHora()).diagnostico(e.getDiagnostico()).tratamientoRealizado(e.getTratamientoRealizado()).observaciones(e.getObservaciones()).build();
        });

        EvolucionClinicaResponseDTO response = historiaClinicaService.registrarEvolucion(5L, request, "dr.house");

        assertEquals(200L, response.getId());
        assertEquals(100L, response.getTurnoId());
        assertEquals(EstadoTurno.ATENDIDO, turno.getEstado());
        verify(turnoRepository).save(turno);
    }

    @Test
    void registrarEvolucion_sinTurno_creaEvolucionCorrectamente() {
        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder().diagnostico("Control postoperatorio").tratamientoRealizado("Limpieza").build();

        when(odontologoRepository.findByUsuarioUsername("dr.house")).thenReturn(Optional.of(odontologo));
        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(evolucionClinicaRepository.save(any(EvolucionClinica.class))).thenAnswer(inv -> {
            EvolucionClinica e = inv.getArgument(0);
            return EvolucionClinica.builder().id(201L).historiaClinica(e.getHistoriaClinica()).odontologo(e.getOdontologo())
                    .fechaHora(e.getFechaHora()).diagnostico(e.getDiagnostico()).tratamientoRealizado(e.getTratamientoRealizado()).build();
        });

        EvolucionClinicaResponseDTO response = historiaClinicaService.registrarEvolucion(5L, request, "dr.house");

        assertEquals(201L, response.getId());
        assertNull(response.getTurnoId());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void registrarEvolucion_turnoNoConfirmado_lanzaBadRequest() {
        Turno turnoPendiente = Turno.builder().id(101L).paciente(paciente).odontologo(odontologo).estado(EstadoTurno.PENDIENTE).build();
        turnoPendiente.setActivo(true);

        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder().turnoId(101L).diagnostico("Diag").tratamientoRealizado("Trat").build();

        when(odontologoRepository.findByUsuarioUsername("dr.house")).thenReturn(Optional.of(odontologo));
        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(turnoRepository.findById(101L)).thenReturn(Optional.of(turnoPendiente));
        when(evolucionClinicaRepository.existsByTurnoId(101L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> historiaClinicaService.registrarEvolucion(5L, request, "dr.house"));
    }

    @Test
    void registrarEvolucion_turnoDeOtroPaciente_lanzaBadRequest() {
        Paciente otroPaciente = Paciente.builder().id(99L).build();
        Turno turnoAjeno = Turno.builder().id(102L).paciente(otroPaciente).odontologo(odontologo).estado(EstadoTurno.CONFIRMADO).build();
        turnoAjeno.setActivo(true);

        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder().turnoId(102L).diagnostico("Diag").tratamientoRealizado("Trat").build();

        when(odontologoRepository.findByUsuarioUsername("dr.house")).thenReturn(Optional.of(odontologo));
        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(turnoRepository.findById(102L)).thenReturn(Optional.of(turnoAjeno));

        assertThrows(BadRequestException.class, () -> historiaClinicaService.registrarEvolucion(5L, request, "dr.house"));
    }

    @Test
    void registrarEvolucion_turnoYaEvolucionado_lanzaConflict() {
        Turno turno = Turno.builder().id(103L).paciente(paciente).odontologo(odontologo).estado(EstadoTurno.ATENDIDO).build();
        turno.setActivo(true);

        EvolucionClinicaRequestDTO request = EvolucionClinicaRequestDTO.builder().turnoId(103L).diagnostico("Diag").tratamientoRealizado("Trat").build();

        when(odontologoRepository.findByUsuarioUsername("dr.house")).thenReturn(Optional.of(odontologo));
        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(turnoRepository.findById(103L)).thenReturn(Optional.of(turno));
        when(evolucionClinicaRepository.existsByTurnoId(103L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> historiaClinicaService.registrarEvolucion(5L, request, "dr.house"));
    }

    @Test
    void obtenerHistoriaClinicaPorPaciente_retornaHistoriaYEvoluciones() {
        EvolucionClinica ev = EvolucionClinica.builder().id(300L).historiaClinica(historiaClinica).odontologo(odontologo)
                .fechaHora(LocalDateTime.of(2026, 11, 25, 14, 0)).diagnostico("Gingivitis leve").tratamientoRealizado("Tartrectomía").build();

        when(pacienteRepository.findById(5L)).thenReturn(Optional.of(paciente));
        when(historiaClinicaRepository.findByPacienteId(5L)).thenReturn(Optional.of(historiaClinica));
        when(evolucionClinicaRepository.findByHistoriaClinicaIdConDetalles(50L)).thenReturn(List.of(ev));

        HistoriaClinicaDetalleResponseDTO detalle = historiaClinicaService.obtenerHistoriaClinicaPorPaciente(5L);

        assertEquals(50L, detalle.getId());
        assertEquals(1, detalle.getEvoluciones().size());
    }

    @Test
    void obtenerHistoriaClinicaPorPaciente_inexistente_lanzaNotFound() {
        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> historiaClinicaService.obtenerHistoriaClinicaPorPaciente(999L));
    }
}