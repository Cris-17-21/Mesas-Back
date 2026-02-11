package com.restaurante.resturante.dto.compras;

public record ProveedorMetodoPagoDto(
        Integer idTipoPago,
        String nombreTipoPago, // Read-only for display
        String datosPago) {
}
