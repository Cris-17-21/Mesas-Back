package com.restaurante.resturante.dto.api_facturacion.empresa_facturacion;

import java.time.LocalDateTime;
import java.util.UUID;

public record FacturacionEmpresaResponse(
        UUID id,
        String ruc,
        String razonSocial,
        String nombreComercial,
        String direccionFiscal,
        String ubigeo,
        String departamento,
        String provincia,
        String distrito,
        String logo,
        Boolean entorno,
        String plan,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String accessToken,
        String refreshToken) {

}
