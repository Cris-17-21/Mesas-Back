package com.restaurante.resturante.dto.compras;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoCompraDto(
        Long idPedidoCompra,
        Integer idProveedor,
        String razonSocialProveedor,
        String idUsuario, // UUID String from User entity
        String nombreUsuario,
        LocalDateTime fechaPedido,
        LocalDate fechaEntregaEsperada,
        Integer idTipoPago,
        String nombreTipoPago,
        String referencia,
        String observaciones,
        String estadoPedido,
        BigDecimal totalPedido,
        Boolean aplicaIgv,
        List<DetallePedidoCompraDto> detalles) {
}
