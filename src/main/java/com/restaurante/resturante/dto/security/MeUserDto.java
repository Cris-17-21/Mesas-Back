package com.restaurante.resturante.dto.security;

/**
 * DTO específico para los datos del usuario en la respuesta /me.
 * No contiene información de seguridad como roles o permisos, solo datos de perfil.
 */
public record MeUserDto(
    String id,
    String username,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String tipoDocumento,
    String numeroDocumento,
    String telefono,
    String email,
    String role
) {}
