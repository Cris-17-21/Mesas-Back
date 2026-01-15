package com.restaurante.resturante.dto.security;

import java.util.List;

/**
 * DTO de nivel superior para la respuesta del endpoint /me.
 * Contiene la estructura completa que el frontend necesita al iniciar sesión.
 */
public record MeResponseDto(
    List<MenuModuleDto> navigation,
    MeUserDto user
) {}
