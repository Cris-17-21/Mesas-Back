package com.restaurante.resturante.dto.security;

public record UserAccessDto(
    String id,
    String userId,
    String empresaId,
    String sucursalId
) {}
