package com.restaurante.resturante.dto.security;

public record CreatePermissionDto(
    String name,
    String description,
    String moduleId
) {}
