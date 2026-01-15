package com.restaurante.resturante.service.security;

import java.util.List;

import com.restaurante.resturante.dto.security.CreatePermissionModuleDto;
import com.restaurante.resturante.dto.security.PermissionModuleDto;

public interface IPermissionModuleService {

    List<PermissionModuleDto> findAll();

    PermissionModuleDto findById(String obfuscatedId);

    PermissionModuleDto create(CreatePermissionModuleDto dto);

    PermissionModuleDto update(String obfuscatedId, CreatePermissionModuleDto dto);

    void delete(String obfuscatedId);

    List<PermissionModuleDto> findModulesWithoutChildren();
}
