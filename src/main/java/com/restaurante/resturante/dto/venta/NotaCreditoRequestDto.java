package com.restaurante.resturante.dto.venta;

public record NotaCreditoRequestDto(
        String comprobanteId, // Comprobante a anular/modificar
        String codMotivo, // Catálogo 09 SUNAT (Ej: 01=Anulación de la operación)
        String descripcion // Sustento
) {
}
