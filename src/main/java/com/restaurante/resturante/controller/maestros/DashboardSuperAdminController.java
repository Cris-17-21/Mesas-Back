package com.restaurante.resturante.controller.maestros;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.dashboard.SuperAdminDashboardDto;
import com.restaurante.resturante.repository.maestro.EmpresaRepository;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class DashboardSuperAdminController {

    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<SuperAdminDashboardDto> getDashboardData() {
        long totalEmpresas = empresaRepository.count();
        long activas = empresaRepository.findAllByActiveTrue().size();
        long inactivas = totalEmpresas - activas;

        long totalSucursales = sucursalRepository.findAllByEstadoTrue().size();
        long totalUsuarios = userRepository.count();

        return ResponseEntity.ok(SuperAdminDashboardDto.builder()
                .totalEmpresas(totalEmpresas)
                .empresasActivas(activas)
                .empresasInactivas(inactivas)
                .totalSucursales(totalSucursales)
                .totalUsuarios(totalUsuarios)
                .empresasStats(empresaRepository.getEmpresasUserStats())
                .build());
    }
}
