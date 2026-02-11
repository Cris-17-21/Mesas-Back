package com.restaurante.resturante.dto.maestro;

public record ClienteDto(
    String id,
    String tipoDocumento,
    String numeroDocumento,
    String nombreRazonSocial,
    String direccion,
    String correo,
    String telefono
) {}
