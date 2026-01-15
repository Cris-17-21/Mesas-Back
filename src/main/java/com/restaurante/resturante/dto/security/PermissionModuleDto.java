package com.restaurante.resturante.dto.security;

import java.util.Set;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

public record PermissionModuleDto(
    @ObfuscatedId 
    Long id,
    String name,
    int displayOrder,
    String urlPath,
    String iconName,
    PermissionModuleDto parent,
    Set<PermissionDto> permissions
) {}
