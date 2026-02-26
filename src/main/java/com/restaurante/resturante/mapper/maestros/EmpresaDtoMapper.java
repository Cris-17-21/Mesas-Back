package com.restaurante.resturante.mapper.maestros;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.maestro.CreateEmpresaDto;
import com.restaurante.resturante.dto.maestro.EmpresaDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmpresaDtoMapper {

    private final SucursalDtoMapper sucursalMapper;

    // Convierte de Entidad a DTO (Para respuestas de API)
    public EmpresaDto toDto(Empresa empresa) {
        if (empresa == null)
            return null;

        return new EmpresaDto(
                empresa.getId(),
                empresa.getRuc(),
                empresa.getRazonSocial(),
                empresa.getDireccionFiscal(),
                empresa.getTelefono(),
                empresa.getEmail(),
                empresa.getLogoUrl(),
                empresa.getFechaAfiliacion() != null ? empresa.getFechaAfiliacion().toString() : null,
                empresa.getSucursales() != null
                        ? empresa.getSucursales().stream().map(sucursalMapper::toDto).collect(Collectors.toList())
                        : Collections.emptyList());
    }

    // Convierte de CreateEmpresaDto a Entidad (Para guardado)
    public Empresa toEntity(CreateEmpresaDto dto) {
        if (dto == null)
            return null;

        // Validación preventiva para evitar Nulos
        String rucSeguro = Objects.requireNonNull(dto.ruc(), "El RUC es requerido.");
        String razonSocialSeguro = Objects.requireNonNull(dto.razonSocial(), "La Razón Social es requerida.");

        return Empresa.builder()
                .ruc(rucSeguro)
                .razonSocial(razonSocialSeguro.toUpperCase())
                .direccionFiscal(dto.direccionFiscal())
                .telefono(dto.telefono())
                .email(dto.email())
                .logoUrl(dto.logoUrl())
                .fechaAfiliacion(
                        dto.fechaAfiliacion() != null ? LocalDate.parse(dto.fechaAfiliacion()) : LocalDate.now())
                .active(true)
                .build();
    }

    // Actualizar entidad
    public void updateEntityFromDto(CreateEmpresaDto dto, Empresa entity) {
        if (dto == null || entity == null)
            return;

        if (dto.ruc() != null && !dto.ruc().isBlank()) {
            entity.setRuc(dto.ruc());
        }

        if (dto.razonSocial() != null && !dto.razonSocial().isBlank()) {
            entity.setRazonSocial(dto.razonSocial().toUpperCase());
        }

        if (dto.direccionFiscal() != null)
            entity.setDireccionFiscal(dto.direccionFiscal());
        if (dto.telefono() != null)
            entity.setTelefono(dto.telefono());
        if (dto.email() != null)
            entity.setEmail(dto.email());
        if (dto.logoUrl() != null)
            entity.setLogoUrl(dto.logoUrl());
    }
}
