package com.restaurante.resturante.dto.security;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY) 
public record MenuModuleDto(
    String name,
    String order,
    String urlPath,
    String iconName,
    List<MenuPermissionDto> permissions,
    List<MenuModuleDto> children
) {}
