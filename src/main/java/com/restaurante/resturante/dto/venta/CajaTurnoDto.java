package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaTurnoDto(
    String id,
    BigDecimal montoApertura,
    LocalDateTime fechaApertura,
    String estado,
    String nombreUsuario
) {}
