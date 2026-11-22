package com.odontologia.controller;

import com.odontologia.dto.HealthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResponseDTO> checkHealth() {
        HealthResponseDTO response = HealthResponseDTO.builder()
                .status("UP")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
