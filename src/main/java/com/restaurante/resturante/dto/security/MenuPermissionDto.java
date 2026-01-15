package com.restaurante.resturante.dto.security;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

/**
 * DTO simple para representar un permiso dentro de la estructura del menú.
 */
public record MenuPermissionDto(
    @ObfuscatedId
    Long id,
    String name
) {}
