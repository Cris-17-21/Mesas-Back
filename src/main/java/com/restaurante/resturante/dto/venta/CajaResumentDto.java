package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaResumentDto(
    String id,
    BigDecimal montoApertura,
    BigDecimal totalVentasEfectivo,
    BigDecimal totalVentasTarjeta,
    BigDecimal totalOtrosMedios,
    BigDecimal totalEsperado,
    LocalDateTime fechaApertura
) {}
