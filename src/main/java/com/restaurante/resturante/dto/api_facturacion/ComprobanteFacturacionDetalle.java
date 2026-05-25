package com.restaurante.resturante.dto.api_facturacion;

import java.math.BigDecimal;

public record ComprobanteFacturacionDetalle(
        String codigo,
        String descripcion,
        @com.fasterxml.jackson.annotation.JsonProperty("unidad") String unidadMedida,
        BigDecimal cantidad,
        BigDecimal mtoValorUnitario,
        BigDecimal mtoPrecioUnitario,
        String tipAfeIgv,
        BigDecimal porcentajeIgv,
        BigDecimal mtoIgv,
        BigDecimal mtoBaseIgv,
        BigDecimal mtoValorVenta) {
}
