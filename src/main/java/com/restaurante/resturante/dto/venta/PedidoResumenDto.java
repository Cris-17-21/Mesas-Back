package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumenDto(
    String id,
    String codigoPedido,
    String estado,
    String tipoEntrega,
    BigDecimal totalFinal,
    LocalDateTime fechaCreacion,
    String nombreCliente,
    String codigoMesa,
    String mesaId
) {}
