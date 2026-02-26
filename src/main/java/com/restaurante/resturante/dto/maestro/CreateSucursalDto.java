package com.restaurante.resturante.dto.maestro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateSucursalDto(
        @NotBlank(message = "El nombre de la Sucursal es requerido.") String nombre,
        String direccion,
        @Pattern(regexp = "^[0-9]{9}$", message = "El telefono debe tener 9 digitos.") String telefono,
        @NotBlank(message = "El id de la Empresa es requerido.") String empresaId) {
}
