package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record AbrirCajaDto(
    BigDecimal montoApertura,
    BigDecimal montoAperturaEfectivo,
    BigDecimal montoAperturaVirtual,
    String sucursalId,
    String usuarioId
) {}
