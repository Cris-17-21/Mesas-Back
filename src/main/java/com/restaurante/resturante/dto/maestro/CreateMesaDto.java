package com.restaurante.resturante.dto.maestro;

public record CreateMesaDto(
    String codigoMesa,
    Integer capacidad,
    Boolean active,
    String pisoId
) {}
