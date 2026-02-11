package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record AbrirCajaDto(
    BigDecimal montoApertura,
    String sucursalId,
    String usuarioId
) {}
