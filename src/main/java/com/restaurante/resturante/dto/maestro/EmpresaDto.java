package com.restaurante.resturante.dto.maestro;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

public record EmpresaDto(
    @ObfuscatedId
    Long id,
    String ruc,
    String razonSocial,
    String direccionFiscal,
    String telefono,
    String email,
    String logoUrl,
    String fechaAfiliacion,
    Double precioMensual
) {}
