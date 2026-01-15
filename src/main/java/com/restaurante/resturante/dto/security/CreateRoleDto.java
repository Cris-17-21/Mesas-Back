package com.restaurante.resturante.dto.security;

import java.util.Set;

public record CreateRoleDto(
    String name,
    String description,
    Set<String> permissionIds 
) {}
