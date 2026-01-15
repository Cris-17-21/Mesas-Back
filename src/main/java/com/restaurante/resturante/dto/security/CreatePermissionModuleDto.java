package com.restaurante.resturante.dto.security;

public record CreatePermissionModuleDto(
    String name,
    int displayOrder,
    String urlPath,
    String iconName,
    String parentId
) {}
