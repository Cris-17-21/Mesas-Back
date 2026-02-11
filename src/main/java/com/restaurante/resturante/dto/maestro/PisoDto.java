package com.restaurante.resturante.dto.maestro;

import java.util.List;

public record PisoDto(
    String id,
    String nombre,
    String descripcion,
    String sucursal,
    List<MesaResponseDto> mesas
) {}
