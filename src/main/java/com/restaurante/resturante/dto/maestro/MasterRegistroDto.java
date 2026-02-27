package com.restaurante.resturante.dto.maestro;

import com.restaurante.resturante.dto.security.CreateUserDto;

import jakarta.validation.Valid;

public record MasterRegistroDto(
        @Valid CreateEmpresaDto empresa,
        @Valid CreateSucursalDto sucursal,
        @Valid CreateUserDto user) {
}
