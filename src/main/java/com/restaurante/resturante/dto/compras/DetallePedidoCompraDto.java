package com.restaurante.resturante.dto.compras;

import java.math.BigDecimal;

public record DetallePedidoCompraDto(
        Long idDetallePedido,
        Integer idProducto,
        String nombreProducto,
        Integer cantidadPedida,
        BigDecimal costoUnitario,
        BigDecimal subtotalLinea,
        Integer cantidadRecibida) {
}
