package com.restaurante.resturante.dto.inventario;

import java.time.LocalDateTime;

public record MovimientoInventarioDto(
    Long idMovimiento,
    Integer idProducto,
    String nombreProducto,
    String sucursalId,
    String tipoMovimiento,
    Integer cantidad,
    String motivo,
    LocalDateTime fechaMovimiento,
    String usuarioId,
    String comprobante
) {
}
