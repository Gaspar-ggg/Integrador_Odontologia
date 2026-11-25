package com.odontologia.repository;

import com.odontologia.model.EvolucionClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvolucionClinicaRepository extends JpaRepository<EvolucionClinica, Long> {

    @Query("SELECT e FROM EvolucionClinica e " +
           "JOIN FETCH e.odontologo o " +
           "LEFT JOIN FETCH e.turno t " +
           "WHERE e.historiaClinica.id = :historiaClinicaId " +
           "ORDER BY e.fechaHora DESC, e.id DESC")
    List<EvolucionClinica> findByHistoriaClinicaIdConDetalles(@Param("historiaClinicaId") Long historiaClinicaId);

    boolean existsByTurnoId(Long turnoId);

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void delete(EvolucionClinica entity) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAll(Iterable<? extends EvolucionClinica> entities) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAllInBatch(Iterable<EvolucionClinica> entities) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAllByIdInBatch(Iterable<Long> ids) {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }

    @Override
    default void deleteAllInBatch() {
        throw new UnsupportedOperationException("RN-PA-03: Las evoluciones clínicas son inmutables y no pueden eliminarse");
    }
}