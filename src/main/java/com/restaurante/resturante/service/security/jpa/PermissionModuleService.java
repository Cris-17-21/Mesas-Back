package com.restaurante.resturante.service.security.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.domain.security.PermissionModule;
import com.restaurante.resturante.dto.security.CreatePermissionModuleDto;
import com.restaurante.resturante.dto.security.PermissionModuleDto;
import com.restaurante.resturante.mapper.security.PermissionModuleDtoMapper;
import com.restaurante.resturante.repository.security.PermissionModuleRepository;
import com.restaurante.resturante.service.security.IPermissionModuleService;
import com.restaurante.resturante.service.security.IdEncryptionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionModuleService implements IPermissionModuleService{

    private final PermissionModuleRepository moduleRepository;
    private final PermissionModuleDtoMapper mapper;
    private final IdEncryptionService idEncryptionService;

    @Override
    public List<PermissionModuleDto> findAll() {
        return moduleRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionModuleDto findById(String obfuscatedId) {
        PermissionModule module = getModuleOrThrow(obfuscatedId);
        return mapper.toDto(module);
    }

    @Override
    @Transactional
    public PermissionModuleDto create(CreatePermissionModuleDto dto) {
        validateDto(dto);

        if (moduleRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe un módulo con nombre '" + dto.name() + "'");
        }

        PermissionModule parent = null;
        if (dto.parentId() != null && !dto.parentId().isBlank()) {
            String parentId = dto.parentId();
            parent = moduleRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Módulo padre no encontrado: " + dto.parentId()));
        }

        PermissionModule module = mapper.toEntity(dto, parent);
        PermissionModule saved = moduleRepository.save(module);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public PermissionModuleDto update(String obfuscatedId, CreatePermissionModuleDto dto) {
        validateDto(dto);

        PermissionModule existing = getModuleOrThrow(obfuscatedId);

        if (!existing.getName().equals(dto.name()) && moduleRepository.existsByName(dto.name())) {
            throw new IllegalStateException("Ya existe otro módulo con nombre '" + dto.name() + "'");
        }

        PermissionModule parent = null;
        if (dto.parentId() != null && !dto.parentId().isBlank()) {
            String parentId = dto.parentId();
            parent = moduleRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Módulo padre no encontrado: " + dto.parentId()));
        }

        existing.setName(dto.name());
        existing.setDisplayOrder(dto.displayOrder());
        existing.setUrlPath(dto.urlPath());
        existing.setIconName(dto.iconName());
        existing.setParent(parent);

        PermissionModule updated = moduleRepository.save(existing);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(String obfuscatedId) {
        PermissionModule existing = getModuleOrThrow(obfuscatedId);

        if (moduleRepository.existsByPermissions_Module_Id(existing.getId())) {
            throw new IllegalStateException("No se puede eliminar el módulo porque tiene permisos asociados.");
        }

        if (moduleRepository.existsByParent_Id(existing.getId())) {
            throw new IllegalStateException("No se puede eliminar el módulo porque tiene módulos hijos.");
        }

        moduleRepository.delete(existing);
    }

    @Override
    public List<PermissionModuleDto> findModulesWithoutChildren() {
        return moduleRepository.findByChildrenIsEmpty()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    private PermissionModule getModuleOrThrow(String obfuscatedId) {
        String id = obfuscatedId;
        return moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PermissionModule no encontrado"));
    }

    private void validateDto(CreatePermissionModuleDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO no puede ser nulo");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.displayOrder() < 0) {
            throw new IllegalArgumentException("El displayOrder debe ser >= 0");
        }
    }
}
