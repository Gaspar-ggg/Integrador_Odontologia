package com.odontologia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String matricula;
    private String especialidad;
    private String username;
    private String email;
    private Boolean activo;
}
