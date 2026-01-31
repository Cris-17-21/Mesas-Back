package com.restaurante.resturante.mapper.maestros;

import java.time.LocalDate;
import java.util.Collections;
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

    /**
     * Convierte de Entidad a DTO (Para respuestas de API)
     */
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
                // Importante: Mapeamos la lista de sucursales a DTOs, no enviamos la entidad
                // pura
                empresa.getSucursales() != null
                        ? empresa.getSucursales().stream().map(sucursalMapper::toDto).collect(Collectors.toList())
                        : Collections.emptyList());
    }

    /**
     * Convierte de CreateEmpresaDto a Entidad (Para guardado)
     */
    public Empresa toEntity(CreateEmpresaDto dto) {
        if (dto == null)
            return null;

        return Empresa.builder()
                .ruc(dto.ruc())
                .razonSocial(dto.razonSocial())
                .direccionFiscal(dto.direccionFiscal())
                .telefono(dto.telefono())
                .email(dto.email())
                .logoUrl(dto.logoUrl())
                .fechaAfiliacion(
                        dto.fechaAfiliacion() != null ? LocalDate.parse(dto.fechaAfiliacion()) : LocalDate.now())
                .active(true)
                .build();
    }
}
