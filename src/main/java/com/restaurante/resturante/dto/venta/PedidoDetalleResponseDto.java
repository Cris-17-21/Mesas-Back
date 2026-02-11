package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record PedidoDetalleResponseDto(
    String id,
    String productoNombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal totalLinea,
    String estadoPreparacion,
    String observaciones
) {}
