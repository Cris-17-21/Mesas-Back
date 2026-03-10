package com.restaurante.resturante.dto.maestro;

public record MedioPagoDto(
        String id,
        String nombre,
        boolean esEfectivo,
        String empresa) {
}
