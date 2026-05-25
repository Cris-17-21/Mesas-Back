package com.restaurante.resturante.dto.api_facturacion;

import java.time.LocalDateTime;
import java.util.UUID;

public record SucursalFacturacionResponse(
    UUID id,
    UUID companyId,
    String nombre,
    String direccion,
    String ubigeo,
    String departamento,
    String provincia,
    String distrito,
    String telefono,
    String correo,
    Boolean esPrincipal,
    Boolean activo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
