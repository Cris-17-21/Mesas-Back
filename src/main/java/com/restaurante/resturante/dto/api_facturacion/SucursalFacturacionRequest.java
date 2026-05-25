package com.restaurante.resturante.dto.api_facturacion;

public record SucursalFacturacionRequest(
    String nombre,
    String direccion,
    String departamento,
    String provincia,
    String distrito,
    String ubigeo,
    String telefono,
    String correo) {}
