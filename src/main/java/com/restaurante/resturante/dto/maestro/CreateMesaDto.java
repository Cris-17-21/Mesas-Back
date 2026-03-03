package com.restaurante.resturante.dto.maestro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMesaDto(
        @NotBlank(message = "El codigo de la mesa es requerido") String codigoMesa,
        @NotNull(message = "La capacidad de la mesa es requerida") Integer capacidad,
        @NotNull(message = "El estado de la mesa es requerido") Boolean active,
        @NotBlank(message = "El piso de la mesa es requerido") String pisoId) {
}
