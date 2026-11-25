package com.odontologia.repository;

import com.odontologia.model.Odontologo;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query(value = "SELECT * FROM odontologos WHERE matricula = :matricula", nativeQuery = true)
    Optional<Odontologo> findByMatriculaIncludingInactive(@Param("matricula") String matricula);
}
