package com.restaurante.resturante.dto.security;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionDto(
    String id,
    String name,
    String description,
    PermissionModuleDto module
) {}
