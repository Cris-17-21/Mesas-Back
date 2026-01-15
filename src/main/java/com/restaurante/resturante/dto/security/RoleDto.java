package com.restaurante.resturante.dto.security;

import java.util.Set;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

public record RoleDto(
    @ObfuscatedId Long id,
    String name,
    String description,
    Set<PermissionDto> permissions
) {}
