package com.odontologia.service;

import com.odontologia.dto.PacienteRequestDTO;
import com.odontologia.dto.PacienteResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.ResourceNotFoundException;
import com.odontologia.model.HistoriaClinica;
import com.odontologia.model.Paciente;
import com.odontologia.repository.HistoriaClinicaRepository;
import com.odontologia.repository.PacienteRepository;
import com.odontologia.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private PacienteRequestDTO request;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        request = PacienteRequestDTO.builder()
                .dni("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefono("1122334455")
                .email("juan@perez.com")
                .build();

        paciente = Paciente.builder()
                .id(1L)
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .build();
    }

    @Test
    void crearPaciente_Exitoso() {
        when(pacienteRepository.existsByDni(anyString())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteResponseDTO response = pacienteService.crearPaciente(request);

        assertNotNull(response);
        assertEquals(request.getDni(), response.getDni());
        verify(pacienteRepository).save(any(Paciente.class));
        verify(historiaClinicaRepository).save(any(HistoriaClinica.class));
    }

    @Test
    void crearPaciente_ConflictException_DniDuplicado() {
        when(pacienteRepository.existsByDni(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> pacienteService.crearPaciente(request));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_Exitoso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO response = pacienteService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void obtenerPorId_ResourceNotFoundException() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.obtenerPorId(1L));
    }

    @Test
    void buscarPacientes_Filtros() {
        when(pacienteRepository.buscarAgil("123", "Juan")).thenReturn(List.of(paciente));

        List<PacienteResponseDTO> results = pacienteService.buscarPacientes("123", "Juan");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void eliminarPaciente_Exitoso() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        pacienteService.eliminarPaciente(1L);

        verify(pacienteRepository).deleteById(1L);
    }
}
