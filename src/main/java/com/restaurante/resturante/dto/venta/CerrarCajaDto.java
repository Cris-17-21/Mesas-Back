package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;

public record CerrarCajaDto(
        String id,
        BigDecimal efectivoReal,
        BigDecimal tarjetaReal,
        String comentario) {
    public BigDecimal montoCierreReal() {
        // En un cierre simple, asumimos que el total real es la suma,
        // o si el DTO solo trae efectivo, retornamos efectivo.
        // Dado el servicio, parece esperar un total único.
        return efectivoReal != null ? efectivoReal : BigDecimal.ZERO;
    }
}
