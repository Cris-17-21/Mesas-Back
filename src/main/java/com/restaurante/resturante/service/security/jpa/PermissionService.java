package com.restaurante.resturante.service.security.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.Permission;
import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.dto.security.CreatePermissionDto;
import com.restaurante.resturante.dto.security.PermissionDto;
import com.restaurante.resturante.mapper.security.PermissionDtoMapper;
import com.restaurante.resturante.repository.security.PermissionModuleRepository;
import com.restaurante.resturante.repository.security.PermissionRepository;
import com.restaurante.resturante.service.security.IPermissionService;
import com.restaurante.resturante.service.security.IdEncryptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService{

    private final PermissionRepository permissionRepository;
    private final PermissionModuleRepository moduleRepository;
    private final PermissionDtoMapper mapper;
    private final IdEncryptionService idEncryptionService;

    @Override
    public List<PermissionDto> findAll() {
        return permissionRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDto findById(String obfuscatedId) {
        Permission permission = getPermissionOrThrow(obfuscatedId);
        return mapper.toDto(permission);
    }

    @Override
    @Transactional
    public PermissionDto create(CreatePermissionDto dto) {
        validateDto(dto);

        if (permissionRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe un permiso con nombre '" + dto.name() + "'");
        }

        // Usar IdEncryptionService para desofuscar el módulo
        long moduleId = idEncryptionService.decrypt(dto.moduleId());
        PermissionModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module no encontrado: " + dto.moduleId()));

        Permission permission = mapper.toEntity(dto, module);
        Permission saved = permissionRepository.save(permission);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PermissionDto update(String obfuscatedId, CreatePermissionDto dto) {
        validateDto(dto);

        Permission existing = getPermissionOrThrow(obfuscatedId);

        if (!existing.getName().equals(dto.name()) && permissionRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe otro permiso con nombre '" + dto.name() + "'");
        }

        // Desofuscar módulo también aquí
        long moduleId = idEncryptionService.decrypt(dto.moduleId());
        PermissionModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module no encontrado: " + dto.moduleId()));

        existing.setName(dto.name());
        existing.setDescription(dto.description());
        existing.setModule(module);

        Permission updated = permissionRepository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(String obfuscatedId) {
        Permission existing = getPermissionOrThrow(obfuscatedId);
        permissionRepository.delete(existing);
    }

    private Permission getPermissionOrThrow(String obfuscatedId) {
        long id = idEncryptionService.decrypt(obfuscatedId);
        return permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission no encontrada"));
    }

    private void validateDto(CreatePermissionDto dto) {
        if (dto == null) throw new IllegalArgumentException("DTO no puede ser nulo");
        if (dto.name() == null || dto.name().isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.description() == null || dto.description().isBlank()) throw new IllegalArgumentException("La descripción es obligatoria");
        if (dto.moduleId() == null || dto.moduleId().isBlank()) throw new IllegalArgumentException("El módulo es obligatorio");
    }
}
