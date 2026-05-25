package com.restaurante.resturante.dto.api_facturacion;

import java.util.UUID;

public record TokenResponseFacturacion(
        String accessToken,
        String refreshToken,
        String tipo,
        UUID idUser,
        String nombre,
        String email,
        String rol,
        UUID idCompany,
        String razonSocial) {
}
