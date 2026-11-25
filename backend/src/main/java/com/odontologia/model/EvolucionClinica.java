package com.odontologia.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
@Entity
@Table(
        name = "evoluciones_clinicas",
        indexes = {
                @Index(name = "idx_evolucion_historia_fecha", columnList = "historia_clinica_id, fecha_hora"),
                @Index(name = "idx_evolucion_odontologo", columnList = "odontologo_id")
        }
)
public class EvolucionClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_clinica_id", nullable = false, updatable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "odontologo_id", nullable = false, updatable = false)
    private Odontologo odontologo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", unique = true, updatable = false)
    private Turno turno;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String diagnostico;

    @Column(name = "tratamiento_realizado", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String tratamientoRealizado;

    @Column(name = "observaciones", columnDefinition = "TEXT", updatable = false)
    private String observaciones;

    @PreUpdate
    private void preventUpdate() {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no admiten modificaciones");
    }

    @PreRemove
    private void preventDelete() {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no admiten eliminación");
    }
}