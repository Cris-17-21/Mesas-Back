package com.restaurante.resturante.mapper.security;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.dto.security.CreatePermissionDto;
import com.restaurante.resturante.dto.security.PermissionDto;
import com.restaurante.resturante.dto.security.PermissionModuleDto;

@Component
public class PermissionDtoMapper {

    public PermissionDtoMapper(PermissionModuleDtoMapper moduleMapper) {
    }

    public PermissionDto toDto(Permission permission) {
        if (permission == null) return null;
        PermissionModuleDto moduleDto = permission.getModule() != null 
                ? new PermissionModuleDto(
                        permission.getModule().getId(),
                        permission.getModule().getName(),
                        permission.getModule().getDisplayOrder(),
                        permission.getModule().getUrlPath(),
                        permission.getModule().getIconName(),
                        null, null
                )
                : null;

        return new PermissionDto(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                moduleDto
        );
    }

    public Permission toEntity(CreatePermissionDto dto, PermissionModule module) {
        if (dto == null) return null;
        Permission permission = new Permission();
        permission.setName(dto.name());
        permission.setDescription(dto.description());
        permission.setModule(module);
        return permission;
    }

    public Permission updateEntity(Permission existing, CreatePermissionDto dto, PermissionModule module) {
        if (dto.name() != null && !dto.name().isBlank()) existing.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) existing.setDescription(dto.description());
        if (module != null) existing.setModule(module);
        return existing;
    }
}
