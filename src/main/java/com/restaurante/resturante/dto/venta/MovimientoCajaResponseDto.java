package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponseDto(
        String id,
        String tipo,
        BigDecimal monto,
        String descripcion,
        LocalDateTime fecha,
        String usuarioNombre) {
}
