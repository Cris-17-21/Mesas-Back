package com.restaurante.resturante.dto.security;

public record CreateUserDto(
    String username,
    String password,
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
