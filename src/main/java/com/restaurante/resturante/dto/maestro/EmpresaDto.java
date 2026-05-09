package com.restaurante.resturante.dto.maestro;

import java.util.List;

public record EmpresaDto(
                String id,
                String ruc,
                String razonSocial,
                String nombreComercial,
                String direccionFiscal,
                String ubigeo,
                String provincia,
                String departamento,
                String distrito,
                String telefono,
                String email,
                String logoUrl,
                String fechaAfiliacion,
                String usuarioSol,
                String claveSol,
                String claveCertificado,
                Boolean entorno,
                String certificadoDigital,
                List<SucursalDto> sucursales) {
}
