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
                empresa.getNombreComercial(),
                empresa.getDireccionFiscal(),
                empresa.getUbigeo(),
                empresa.getProvincia(),
                empresa.getDepartamento(),
                empresa.getDistrito(),
                empresa.getTelefono(),
                empresa.getEmail(),
                empresa.getLogoUrl() != null ? new String(empresa.getLogoUrl(), java.nio.charset.StandardCharsets.UTF_8)
                        : null,
                empresa.getFechaAfiliacion() != null ? empresa.getFechaAfiliacion().toString() : null,
                empresa.getUsuarioSol(),
                empresa.getClaveSol(),
                empresa.getClaveCertificado(),
                empresa.getEntorno(),
                empresa.getCertificadoDigital(),
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
                .nombreComercial(dto.nombreComercial())
                .direccionFiscal(dto.direccionFiscal())
                .ubigeo(dto.ubigeo())
                .provincia(dto.provincia())
                .departamento(dto.departamento())
                .distrito(dto.distrito())
                .telefono(dto.telefono())
                .email(dto.email())
                .logoUrl(dto.logoUrl() != null ? dto.logoUrl().getBytes(java.nio.charset.StandardCharsets.UTF_8) : null)
                .fechaAfiliacion(
                        dto.fechaAfiliacion() != null ? LocalDate.parse(dto.fechaAfiliacion()) : LocalDate.now())
                .usuarioSol(dto.usuarioSol())
                .claveSol(dto.claveSol())
                .claveCertificado(dto.claveCertificado())
                .entorno(dto.entorno() != null ? dto.entorno() : false)
                .certificadoDigital(dto.certificadoDigital())
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
        if (dto.nombreComercial() != null)
            entity.setNombreComercial(dto.nombreComercial());
        if (dto.ubigeo() != null)
            entity.setUbigeo(dto.ubigeo());
        if (dto.provincia() != null)
            entity.setProvincia(dto.provincia());
        if (dto.departamento() != null)
            entity.setDepartamento(dto.departamento());
        if (dto.distrito() != null)
            entity.setDistrito(dto.distrito());
        if (dto.telefono() != null)
            entity.setTelefono(dto.telefono());
        if (dto.email() != null)
            entity.setEmail(dto.email());
        if (dto.logoUrl() != null)
            entity.setLogoUrl(dto.logoUrl().getBytes(java.nio.charset.StandardCharsets.UTF_8));

        if (dto.usuarioSol() != null)
            entity.setUsuarioSol(dto.usuarioSol());
        if (dto.claveSol() != null)
            entity.setClaveSol(dto.claveSol());
        if (dto.claveCertificado() != null)
            entity.setClaveCertificado(dto.claveCertificado());
        if (dto.entorno() != null)
            entity.setEntorno(dto.entorno());
        if (dto.certificadoDigital() != null)
            entity.setCertificadoDigital(dto.certificadoDigital());
    }
}
