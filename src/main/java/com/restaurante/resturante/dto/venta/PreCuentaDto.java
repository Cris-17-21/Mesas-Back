package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PreCuentaDto(
        String id,
        String codigoPedido,
        String mesaNombre,
        String mozoNombre,
        LocalDateTime fecha,
        List<PreCuentaDetalleDto> detalles,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal totalFinal) {
    public record PreCuentaDetalleDto(
            String productoNombre,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal totalLinea) {
    }
}
