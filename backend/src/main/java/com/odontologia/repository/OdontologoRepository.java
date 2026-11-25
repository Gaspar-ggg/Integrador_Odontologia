package com.odontologia.repository;

import com.odontologia.model.Odontologo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdontologoRepository extends JpaRepository<Odontologo, Long> {

    Optional<Odontologo> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);

    List<Odontologo> findByEspecialidad(String especialidad);

    List<Odontologo> findByActivoTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Odontologo o WHERE o.id = :id")
    Optional<Odontologo> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT * FROM odontologos WHERE matricula = :matricula", nativeQuery = true)
    Optional<Odontologo> findByMatriculaIncludingInactive(@Param("matricula") String matricula);
}
