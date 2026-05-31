package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record PagoMixtoItemDto(
    String medioPagoId,
    BigDecimal monto
) {}
