package com.restaurante.resturante.dto.venta;

public record PedidoDetalleRequestDto(
    String productoId,
    Integer cantidad,
    String observaciones
) {}
