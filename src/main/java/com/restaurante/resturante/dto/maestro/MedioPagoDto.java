package com.restaurante.resturante.dto.maestro;

public record MedioPagoDto(
    String id,
    String nombre,
    boolean esEfectivo,
    boolean requiereReferencia,
    String codigoSunat,
    boolean isActive,
    String empresaId
) {}
