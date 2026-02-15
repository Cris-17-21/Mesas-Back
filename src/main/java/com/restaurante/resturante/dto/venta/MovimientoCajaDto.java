package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record MovimientoCajaDto(
        String cajaId,
        String tipo, // INGRESO, EGRESO
        BigDecimal monto,
        String descripcion,
        String usuarioId) {
}
