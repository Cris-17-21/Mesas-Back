package com.restaurante.resturante.mapper.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.dto.security.CreatePermissionModuleDto;
import com.restaurante.resturante.dto.security.PermissionDto;
import com.restaurante.resturante.dto.security.PermissionModuleDto;

@Component
public class PermissionModuleDtoMapper {

    public PermissionModuleDtoMapper() {}

    public PermissionModuleDto toDto(PermissionModule module) {
        if (module == null) return null;

        // Evitar NPE en permissions y children
        Set<PermissionDto> permissions = module.getPermissions() != null
                ? module.getPermissions().stream()
                    .map(p -> new PermissionDto(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            null // no incluimos módulo dentro del permiso para evitar ciclo
                    ))
                    .collect(Collectors.toSet())
                : Set.of(); // vacío si es null

        PermissionModuleDto parentDto = module.getParent() != null
                ? new PermissionModuleDto(
                        module.getParent().getId(),
                        module.getParent().getName(),
                        module.getParent().getDisplayOrder(),
                        module.getParent().getUrlPath(),
                        module.getParent().getIconName(),
                        null, // no mapeamos permisos ni hijos del padre para romper ciclo
                        Set.of()
                )
                : null;

        return new PermissionModuleDto(
                module.getId(),
                module.getName(),
                module.getDisplayOrder(),
                module.getUrlPath(),
                module.getIconName(),
                parentDto,
                permissions
        );
    }

    public PermissionModule toEntity(CreatePermissionModuleDto dto, PermissionModule parent) {
        if (dto == null) return null;
        PermissionModule module = new PermissionModule();
        module.setName(dto.name());
        module.setDisplayOrder(dto.displayOrder());
        module.setUrlPath(dto.urlPath());
        module.setIconName(dto.iconName());
        module.setParent(parent);
        return module;
    }
}
