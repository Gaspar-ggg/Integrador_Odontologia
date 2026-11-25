package com.odontologia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoPublicDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String matricula;
    private String especialidad;
}
