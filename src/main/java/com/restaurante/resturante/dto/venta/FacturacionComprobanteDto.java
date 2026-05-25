package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record FacturacionComprobanteDto(
        String id,
        String tipoComprobante, // BOLETA, FACTURA, NOTA_DE_VENTA
        String serie,
        String correlativo,
        String rucEmisor,
        String fechaEmision,
        BigDecimal totalVenta,
        String pedidoId,
        String archivoXml,
        String archivoPdf,
        String archivoTxt) {
}
