package com.restaurante.resturante.service.security;

import java.util.List;

import com.restaurante.resturante.dto.security.CreatePermissionDto;
import com.restaurante.resturante.dto.security.PermissionDto;

public interface IPermissionService {

    List<PermissionDto> findAll();

    PermissionDto findById(String obfuscatedId);

    PermissionDto create(CreatePermissionDto dto);

    PermissionDto update(String obfuscatedId, CreatePermissionDto dto);

    void delete(String obfuscatedId);
}
