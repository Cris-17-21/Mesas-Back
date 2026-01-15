package com.restaurante.resturante.dto.security;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

public record UserDto(
    @ObfuscatedId
    Long id,
    String username,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String tipoDocumento,
    String numeroDocumento,
    String sexo,
    String fechaNacimiento,
    String telefono,
    String direccion,
    String email,
    String role
) {}
