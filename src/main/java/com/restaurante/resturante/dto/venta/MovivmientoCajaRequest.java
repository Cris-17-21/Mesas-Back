package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record MovivmientoCajaRequest(
    String cajaTurnoId,
    BigDecimal monto,
    String descripcion,
    String tipo
) {}
