package com.restaurante.resturante.dto.venta;

public record FacturaRequestDto(
        String pedidoId,
        String tipoComprobante, // BOLETA, FACTURA
        String rucApellidos, // RUC para factura, DNI/Apellidos para boleta
        String razonSocialNombres,
        String direccion) {
}
