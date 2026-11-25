package com.odontologia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OdontologoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no debe superar 100 caracteres")
    private String apellido;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 50, message = "La matrícula no debe superar 50 caracteres")
    private String matricula;

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100, message = "La especialidad no debe superar 100 caracteres")
    private String especialidad;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no debe superar 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no debe superar 100 caracteres")
    private String email;

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 8, max = 100, message = "La password debe tener entre 8 y 100 caracteres")
    private String password;
}
