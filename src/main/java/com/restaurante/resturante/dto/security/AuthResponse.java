package com.restaurante.resturante.dto.security;

import java.sql.Date;

public record AuthResponse(
    String accessToken, 
    String refreshToken,
    Date expirationAccessToken,
    Date expirationRefreshToken
) {}
