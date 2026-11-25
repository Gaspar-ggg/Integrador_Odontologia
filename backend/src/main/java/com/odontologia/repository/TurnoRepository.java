package com.odontologia.repository;

import com.odontologia.model.EstadoTurno;
import com.odontologia.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    @Query("SELECT COUNT(t) > 0 FROM Turno t " +
           "WHERE t.odontologo.id = :odontologoId " +
           "AND t.activo = true " +
           "AND t.estado <> com.odontologia.model.EstadoTurno.CANCELADO " +
           "AND t.fechaHoraInicio < :fin " +
           "AND t.fechaHoraFin > :inicio")
    boolean existsSolapamiento(@Param("odontologoId") Long odontologoId,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin);

    @Query("SELECT t FROM Turno t " +
           "WHERE t.odontologo.id = :odontologoId " +
           "AND t.activo = true " +
           "AND t.estado <> com.odontologia.model.EstadoTurno.CANCELADO " +
           "AND t.fechaHoraInicio < :finDia " +
           "AND t.fechaHoraFin > :inicioDia " +
           "ORDER BY t.fechaHoraInicio ASC")
    List<Turno> findActivosNoCanceladosEnRango(@Param("odontologoId") Long odontologoId,
                                               @Param("inicioDia") LocalDateTime inicioDia,
                                               @Param("finDia") LocalDateTime finDia);

    @Query("SELECT t FROM Turno t " +
           "JOIN FETCH t.paciente p " +
           "JOIN FETCH t.odontologo o " +
           "WHERE t.activo = true " +
           "AND (:odontologoId IS NULL OR o.id = :odontologoId) " +
           "AND (:pacienteId IS NULL OR p.id = :pacienteId) " +
           "AND (:estado IS NULL OR t.estado = :estado) " +
           "AND (:desde IS NULL OR t.fechaHoraInicio >= :desde) " +
           "AND (:hasta IS NULL OR t.fechaHoraInicio < :hasta) " +
           "ORDER BY t.fechaHoraInicio ASC")
    List<Turno> findByFiltros(@Param("odontologoId") Long odontologoId,
                              @Param("pacienteId") Long pacienteId,
                              @Param("estado") EstadoTurno estado,
                              @Param("desde") LocalDateTime desde,
                              @Param("hasta") LocalDateTime hasta);
}
