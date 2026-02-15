package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.util.List;

public record RegistrarPagoDto(
    String pedidoId,
    BigDecimal monto,
    String metodoPago,
    String referencia,
    List<String> detalleIds
) {}
