package com.restaurante.resturante.dto.security;

import java.util.Set;

public record PermissionModuleDto(
    String id,
    String name,
    int displayOrder,
    String urlPath,
    String iconName,
    PermissionModuleDto parent,
    Set<PermissionDto> permissions
) {}
