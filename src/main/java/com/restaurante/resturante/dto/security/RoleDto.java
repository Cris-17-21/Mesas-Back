package com.restaurante.resturante.dto.security;

import java.util.Set;

public record RoleDto(
    String id,
    String name,
    String description,
    Set<PermissionDto> permissions
) {}
