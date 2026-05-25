package com.restaurante.resturante.dto.api_facturacion;

public record ClienteFacturacionResponse(
        String id,
        String tipoDoc,
        String numDoc,
        String razonSocial,
        String direccion,
        String email,
        String telefono,
        Boolean activo) {
}
