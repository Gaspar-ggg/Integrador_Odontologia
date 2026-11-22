package com.odontologia.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Recurso no encontrado con id 99");
        }

        @GetMapping("/test/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Parámetro inválido");
        }

        @GetMapping("/test/conflict")
        public void throwConflict() {
            throw new ConflictException("Conflicto de reserva de turno");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException retornando 404 con estructura ErrorResponseDTO")
    void handleResourceNotFound_DebeRetornar404() throws Exception {
        mockMvc.perform(get("/test/not-found").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado con id 99"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    @DisplayName("Debe manejar BadRequestException retornando 400 con estructura ErrorResponseDTO")
    void handleBadRequest_DebeRetornar400() throws Exception {
        mockMvc.perform(get("/test/bad-request").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parámetro inválido"))
                .andExpect(jsonPath("$.path").value("/test/bad-request"));
    }

    @Test
    @DisplayName("Debe manejar ConflictException retornando 409 con estructura ErrorResponseDTO")
    void handleConflict_DebeRetornar409() throws Exception {
        mockMvc.perform(get("/test/conflict").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Conflicto de reserva de turno"))
                .andExpect(jsonPath("$.path").value("/test/conflict"));
    }
}
