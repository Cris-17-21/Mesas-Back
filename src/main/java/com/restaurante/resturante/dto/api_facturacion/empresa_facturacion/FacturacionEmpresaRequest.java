package com.restaurante.resturante.dto.api_facturacion.empresa_facturacion;

public record FacturacionEmpresaRequest(
        String ruc,
        String razonSocial,
        String nombreComercial,
        String direccionFiscal,
        String ubigeo,
        String departamento,
        String provincia,
        String distrito,
        String usuarioSol,
        String claveSol,
        String clientId,
        String clientSecret,
        String certificado,
        String logo,
        Boolean entorno) {

}
