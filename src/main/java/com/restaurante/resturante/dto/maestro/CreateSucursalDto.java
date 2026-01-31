package com.restaurante.resturante.dto.maestro;

public record CreateSucursalDto(
    String nombre,
    String direccion,
    String telefono,
    String empresaId
) {}
