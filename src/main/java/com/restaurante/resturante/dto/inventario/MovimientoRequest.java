package com.restaurante.resturante.dto.inventario;

public record MovimientoRequest(
    Integer idProducto,
    String sucursalId,
    String tipoMovimiento, // ENTRADA o SALIDA
    Integer cantidad,
    String motivo, // ej: COMPRA, MERMA, AJUSTE
    String usuarioId,
    String comprobante
) {
}
