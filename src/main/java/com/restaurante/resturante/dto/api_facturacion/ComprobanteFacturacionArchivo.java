package com.restaurante.resturante.dto.api_facturacion;

public record ComprobanteFacturacionArchivo(
        String xmlToken,
        String pdfToken,
        String cdrToken) {
}
