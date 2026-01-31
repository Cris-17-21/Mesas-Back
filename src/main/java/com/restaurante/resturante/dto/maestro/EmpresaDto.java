package com.restaurante.resturante.dto.maestro;

import java.util.List;

public record EmpresaDto(
    String id,
    String ruc,
    String razonSocial,
    String direccionFiscal,
    String telefono,
    String email,
    String logoUrl,
    String fechaAfiliacion,
    List<SucursalDto> sucursales 
) {}
