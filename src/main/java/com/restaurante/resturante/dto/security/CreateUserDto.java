package com.restaurante.resturante.dto.security;

public record CreateUserDto(
    String username,
    String password,
    String nombres,
    String apellidoPaterno,
    String apellidoMaterno,
    String tipoDocumento,
    String numeroDocumento,
    String telefono,
    String email,
    String role,
    String empresaId,
    String sucursalId
) {}
