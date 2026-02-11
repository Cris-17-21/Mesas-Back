package com.restaurante.resturante.service.security;

import java.util.List;

import com.restaurante.resturante.dto.security.CreateUserDto;
import com.restaurante.resturante.dto.security.MeResponseDto;
import com.restaurante.resturante.dto.security.UserDto;

public interface IUserService {

    MeResponseDto getUserDetailsForMe(String username);

    UserDto getUserById(String obfuscatedId);

    List<UserDto> getAllUsers();

    UserDto create(CreateUserDto dto);

    UserDto update(String obfuscatedId, CreateUserDto dto);

    void delete(String obfuscatedId);

    List<UserDto> getUserByEmpresaIdAndSucursalId(String empresaId, String sucursalId);

    List<UserDto> getUserByEmpresaId(String empresaId);

    UserDto getUserByUsername(String username);
}
