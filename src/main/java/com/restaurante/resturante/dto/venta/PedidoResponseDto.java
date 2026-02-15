package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDto(
    String id,
    String codigoPedido,
    String estado,
    String tipoEntrega,
    BigDecimal totalFinal,
    LocalDateTime fechaCreacion,
    String nombreCliente,
    String codigoMesa,
    List<PedidoDetalleResponseDto> detalles,
    String sucursalId
) {}
