package com.restaurante.resturante.mapper.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.dto.security.CreateRoleDto;
import com.restaurante.resturante.dto.security.PermissionDto;
import com.restaurante.resturante.dto.security.RoleDto;

@Component
public class RoleDtoMapper {

    private final PermissionDtoMapper permissionMapper;

    public RoleDtoMapper(PermissionDtoMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public RoleDto toDto(Role role) {
        if (role == null) return null;
        Set<PermissionDto> permissions = role.getPermissions().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toSet());

        return new RoleDto(
                Long.valueOf(role.getId()),
                role.getName(),
                role.getDescription(),
                permissions
        );
    }

    public Role toEntity(CreateRoleDto dto, Set<Permission> permissions) {
        if (dto == null) return null;
        Role role = new Role();
        role.setName(dto.name());
        role.setDescription(dto.description());
        role.setPermissions(permissions);
        return role;
    }

    public Role updateEntity(Role existing, CreateRoleDto dto, Set<Permission> permissions) {
        if (dto.name() != null && !dto.name().isBlank()) existing.setName(dto.name());
        if (dto.description() != null) existing.setDescription(dto.description());
        existing.setPermissions(permissions);
        return existing;
    }

    public List<RoleDto> toDto(List<Role> roles) {
        return roles.stream().map(this::toDto).toList();
    }
}
