package com.restaurante.resturante.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaUserStatsDto {
    private String id;
    private String ruc;
    private String razonSocial;
    private String email;
    private String telefono;
    private long cantidadUsuarios;
    private long cantidadSucursales;
}
