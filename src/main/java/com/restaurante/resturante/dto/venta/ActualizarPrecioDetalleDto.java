package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record ActualizarPrecioDetalleDto(
        String detalleId,
        BigDecimal precioUnitario
) {}
