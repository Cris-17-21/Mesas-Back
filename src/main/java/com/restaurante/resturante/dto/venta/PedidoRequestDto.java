package com.restaurante.resturante.dto.venta;

import java.util.List;

public record PedidoRequestDto(
    String tipoEntrega,
    String sucursalId,
    String mesaId,
    String clienteId,
    String usuarioId,
    List<PedidoDetalleRequestDto> detalles
) {}
