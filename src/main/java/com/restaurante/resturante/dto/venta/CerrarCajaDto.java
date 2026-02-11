package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record CerrarCajaDto(
    String id,
    BigDecimal montoCierreReal
) {}
