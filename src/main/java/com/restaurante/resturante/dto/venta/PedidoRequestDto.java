package com.restaurante.resturante.dto.venta;

import java.util.List;

public record PedidoRequestDto(
        String tipoEntrega,
        String sucursalId,
        String mesaId,
        String usuarioId,
        List<PedidoDetalleRequestDto> detalles,
        String nombreCliente,
        String telefono,
        String direccion) {
}
