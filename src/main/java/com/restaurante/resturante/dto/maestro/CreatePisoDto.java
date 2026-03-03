package com.restaurante.resturante.dto.maestro;

import jakarta.validation.constraints.NotBlank;

public record CreatePisoDto(
        @NotBlank(message = "El nombre del piso es requerido.") String nombre,
        String descripcion,
        @NotBlank(message = "La sucursal es requerida.") String sucursalId) {
}
