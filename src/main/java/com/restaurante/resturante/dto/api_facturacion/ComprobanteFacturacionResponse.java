package com.restaurante.resturante.dto.api_facturacion;

import java.math.BigDecimal;

public record ComprobanteFacturacionResponse(
        String id,
        String tipoDoc,
        String serie,
        Integer correlativo,
        String numeroCompleto,
        String fechaEmision,
        String tipoMoneda,
        BigDecimal mtoOperGravadas,
        BigDecimal mtoOperExoneradas,
        BigDecimal mtoOperInafectas,
        BigDecimal mtoIgv,
        BigDecimal mtoImpVenta,
        String estadoSunat,
        String ticketSunat,
        String cdrCodigo,
        String cdrDescripcion,
        String cdrHash,
        ComprobanteFacturacionArchivo archivo) {
}
