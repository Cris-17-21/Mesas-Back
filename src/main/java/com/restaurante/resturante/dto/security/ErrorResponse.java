package com.restaurante.resturante.dto.security;

import java.time.Instant;

public record ErrorResponse(
    int status,
    String error,
    String message,
    Instant timestamp
) {}
