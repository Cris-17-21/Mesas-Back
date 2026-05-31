package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record FacturacionComprobanteDto(
        String id,
        String tipoComprobante,
        String serie,
        String correlativo,
        String rucEmisor,
        String fechaEmision,
        BigDecimal totalVenta,
        String pedidoId,
        String archivoXml,
        String archivoPdf,
        String archivoTxt,
        String estadoSunat,
        String cdrSunatXml,
        String sunatMensajeError,
        Long minutosHastaEnvio,
        Boolean impresionConsumo) {
}
