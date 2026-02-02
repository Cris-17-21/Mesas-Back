package com.restaurante.resturante.dto.compras;

public record ProveedorDto(
        Integer idProveedor,
        String razonSocial,
        String nombreComercial,
        String ruc,
        String direccion,
        String telefono,
        Integer estado) {
}
