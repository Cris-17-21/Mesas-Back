package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record PedidoDetalleResponseDto(
    String id,
    String productoNombre,
    Integer cantidad,
    Integer cantidadPagada, // <-- NUEVO: Vital para separar cuentas
    BigDecimal precioUnitario,
    BigDecimal totalLinea,
    String estadoPreparacion,
    String estadoPago, // PENDIENTE, PARCIAL, PAGADO
    String observaciones
) {}
