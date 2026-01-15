package com.restaurante.resturante.service.security.jpa;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.Role;
import com.restaurante.resturante.dto.security.CreateRoleDto;
import com.restaurante.resturante.dto.security.RoleDto;
import com.restaurante.resturante.mapper.security.RoleDtoMapper;
import com.restaurante.resturante.repository.security.PermissionRepository;
import com.restaurante.resturante.repository.security.RoleRepository;
import com.restaurante.resturante.service.security.IRoleService;
import com.restaurante.resturante.service.security.IdEncryptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleDtoMapper mapper;
    private final IdEncryptionService idEncryptionService;

    @Override
    public List<RoleDto> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto findById(String obfuscatedId) {
        Role role = getRoleOrThrow(obfuscatedId);
        return mapper.toDto(role);
    }

    @Override
    @Transactional
    public RoleDto create(CreateRoleDto dto) {
        validateDto(dto);

        if (roleRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe un rol con nombre '" + dto.name() + "'");
        }

        // Convertir los IDs ofuscados en IDs reales usando IdEncryptionService
        Set<Permission> permissions = dto.permissionIds().stream()
                .map(pid -> {
                    long id = idEncryptionService.decrypt(pid);
                    return permissionRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Permission no encontrada: " + pid));
                })
                .collect(Collectors.toSet());

        Role role = mapper.toEntity(dto, permissions);
        Role saved = roleRepository.save(role);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoleDto update(String obfuscatedId, CreateRoleDto dto) {
        validateDto(dto);

        Role existing = getRoleOrThrow(obfuscatedId);

        if (!existing.getName().equals(dto.name()) && roleRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe otro rol con nombre '" + dto.name() + "'");
        }

        // Convertir los IDs ofuscados en IDs reales usando IdEncryptionService
        Set<Permission> permissions = dto.permissionIds().stream()
                .map(pid -> {
                    long id = idEncryptionService.decrypt(pid);
                    return permissionRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Permission no encontrada: " + pid));
                })
                .collect(Collectors.toSet());

        existing.setName(dto.name());
        existing.setPermissions(permissions);

        Role updated = roleRepository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(String obfuscatedId) {
        Role existing = getRoleOrThrow(obfuscatedId);
        roleRepository.delete(existing);
    }

    private Role getRoleOrThrow(String obfuscatedId) {
        long id = idEncryptionService.decrypt(obfuscatedId);
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role no encontrado"));
    }

    private void validateDto(CreateRoleDto dto) {
        if (dto == null) throw new IllegalArgumentException("DTO no puede ser nulo");
        if (dto.name() == null || dto.name().isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.description() != null && dto.description().length() > 50)
            throw new IllegalArgumentException("La descripción no puede exceder 50 caracteres");
    }
}
