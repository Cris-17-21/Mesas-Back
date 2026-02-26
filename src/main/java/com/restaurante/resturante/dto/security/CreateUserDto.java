package com.restaurante.resturante.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserDto(
        @NotBlank(message = "El usuario es obligatorio.") String username,
        @NotBlank(message = "La contraseña es obligatoria.") String password,
        @NotBlank(message = "Los nombres es obligatorio.") String nombres,
        @NotBlank(message = "El apellido paterno es obligatorio.") String apellidoPaterno,
        @NotBlank(message = "El apellido materno es obligatorio.") String apellidoMaterno,
        @NotBlank(message = "El tipo de documento es obligatorio.") String tipoDocumento,
        @NotBlank(message = "El número de documento es obligatorio.") @Pattern(regexp = "^[0-9]{8}$", message = "El número de documento debe tener 8 dígitos.") String numeroDocumento,
        String telefono,
        String email,
        String role,
        String empresaId,
        String sucursalId) {
}
