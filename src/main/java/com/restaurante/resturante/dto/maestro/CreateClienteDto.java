package com.restaurante.resturante.dto.maestro;

public record CreateClienteDto(
    String numeroDocumento,
    String nombreRazonSocial,
    String direccion,
    String correo,
    String telefono,
    String empresaId,
    String tipoDocumentoId
) {}
