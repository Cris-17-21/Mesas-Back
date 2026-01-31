package com.restaurante.resturante.dto.maestro;

public record CreateEmpresaDto(
    String ruc,
    String razonSocial,
    String direccionFiscal,
    String telefono,
    String email,
    String logoUrl,
    String fechaAfiliacion
) {}
