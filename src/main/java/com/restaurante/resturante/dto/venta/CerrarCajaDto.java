package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record CerrarCajaDto(
        String id,
        BigDecimal efectivoReal,
        BigDecimal tarjetaReal,
        String comentario) {
    public BigDecimal montoCierreReal() {
        BigDecimal cash = efectivoReal != null ? efectivoReal : BigDecimal.ZERO;
        BigDecimal card = tarjetaReal != null ? tarjetaReal : BigDecimal.ZERO;
        return cash.add(card);
    }
}
