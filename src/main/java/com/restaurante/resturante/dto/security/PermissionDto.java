package com.restaurante.resturante.dto.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.restaurante.resturante.config.jackson.ObfuscatedId;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PermissionDto(
    @ObfuscatedId 
    Long id,
    String name,
    String description,
    PermissionModuleDto module
) {}
