package com.restaurante.resturante.dto.dashboard;

import java.util.List;

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
public class SuperAdminDashboardDto {
    private long totalEmpresas;
    private long empresasActivas;
    private long empresasInactivas;
    private long totalSucursales;
    private long totalUsuarios;
    private List<EmpresaUserStatsDto> empresasStats;
}
