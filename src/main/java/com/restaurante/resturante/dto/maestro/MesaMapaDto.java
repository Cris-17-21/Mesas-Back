package com.restaurante.resturante.dto.maestro;

public record MesaMapaDto(
    String id,
    String codigoMesa,
    String estado, // LIBRE, OCUPADA, RESERVADA, UNIDA
    String pedidoId, // Si está ocupada, para ir directo al pedido al darle click
    String nombrePiso,
    Integer capacidad
) {}
