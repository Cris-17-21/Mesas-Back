package com.restaurante.resturante.service.security;

import java.util.List;

import com.restaurante.resturante.dto.security.CreateRoleDto;
import com.restaurante.resturante.dto.security.RoleDto;

public interface IRoleService {

    List<RoleDto> findAll();

    RoleDto findById(String obfuscatedId);

    RoleDto create(CreateRoleDto dto);

    RoleDto update(String obfuscatedId, CreateRoleDto dto);

    void delete(String obfuscatedId);
}
