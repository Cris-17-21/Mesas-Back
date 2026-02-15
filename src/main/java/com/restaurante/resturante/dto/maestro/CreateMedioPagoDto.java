package com.restaurante.resturante.dto.maestro;

public record CreateMedioPagoDto(
    String nombre,
    boolean esEfectivo,
    boolean requiereReferencia,
    String codigoSunat,
    String empresaId
) {}
