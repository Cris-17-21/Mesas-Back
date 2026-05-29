package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record CerrarCajaDto(
        String id,
        BigDecimal efectivoCierreReal,
        BigDecimal virtualCierreReal,
        BigDecimal efectivoCierreEsperado,
        BigDecimal virtualCierreEsperado,
        String comentario) {
    public BigDecimal montoCierreReal() {
        BigDecimal cash = efectivoCierreReal != null ? efectivoCierreReal : BigDecimal.ZERO;
        BigDecimal card = virtualCierreReal != null ? virtualCierreReal : BigDecimal.ZERO;
        return cash.add(card);
    }
}
