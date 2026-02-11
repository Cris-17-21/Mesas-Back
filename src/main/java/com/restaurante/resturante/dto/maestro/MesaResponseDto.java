package com.restaurante.resturante.dto.maestro;

public record MesaResponseDto(
    String id,
    String codigoMesa,
    Integer capacidad,
    String estado,
    String pisoNombre,
    String idPrincipal
) {}
