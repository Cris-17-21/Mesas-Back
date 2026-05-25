package com.restaurante.resturante.dto.api_facturacion;

import java.util.List;

public record ComprobanteFacturacionRequest(
        String tipoDoc,
        String serie,
        String idCliente,
        String idSucursal,
        String tipoOperacion,
        String fechaEmision,
        String fechaVencimiento,
        String tipoMoneda,
        Boolean entorno,
        List<ComprobanteFacturacionDetalle> detalles,
        String tipDocAfectado,
        String numDocAfectado,
        String codMotivo,
        String desMotivo) {
}
