package com.restaurante.resturante.dto.security;

import java.util.Date;
import java.util.List;

import com.restaurante.resturante.dto.maestro.SucursalDto;

public record AuthResponse(
    String accessToken, 
    String refreshToken,
    Date expirationAccessToken,
    Date expirationRefreshToken,
    boolean requireSucursalSelection,
    List<SucursalDto> sucursalesDisponibles
) {}
