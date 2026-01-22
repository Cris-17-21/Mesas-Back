package com.restaurante.resturante.dto.security;

public record UserDto(
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
