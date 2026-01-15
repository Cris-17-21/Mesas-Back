package com.restaurante.resturante.dto.security;

import com.restaurante.resturante.config.jackson.ObfuscatedId;

/**
 * DTO específico para los datos del usuario en la respuesta /me.
 * No contiene información de seguridad como roles o permisos, solo datos de perfil.
 */
public record MeUserDto(
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
