package com.odontologia.repository;

import com.odontologia.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByDni(String dni);

    Optional<Paciente> findByDni(String dni);

    @Query("SELECT p FROM Paciente p WHERE " +
           "(:dni IS NULL OR p.dni = :dni) AND " +
           "(:query IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Paciente> buscarAgil(@Param("dni") String dni, @Param("query") String query);
}
