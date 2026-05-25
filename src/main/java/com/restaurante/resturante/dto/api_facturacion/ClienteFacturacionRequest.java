package com.restaurante.resturante.dto.api_facturacion;

public record ClienteFacturacionRequest(
        String tipoDoc,
        String numDoc,
        String razonSocial,
        String direccion,
        String email,
        String telefono) {
}
