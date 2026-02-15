package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaTurnoDto(
    String id,
    String estado, // "ABIERTO" o "CERRADO"
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    String usuarioNombre,
    BigDecimal montoInicial,
    BigDecimal diferencia
) {}
