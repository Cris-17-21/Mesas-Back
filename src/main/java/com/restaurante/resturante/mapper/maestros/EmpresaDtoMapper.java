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

        String logoBase64 = null;
        if (empresa.getLogoUrl() != null) {
            String mimeType = getMimeType(empresa.getLogoUrl());
            logoBase64 = "data:" + mimeType + ";base64," + java.util.Base64.getEncoder().encodeToString(empresa.getLogoUrl());
        }

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
                logoBase64,
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
                .logoUrl(parseLogoUrl(dto.logoUrl()))
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
            entity.setLogoUrl(parseLogoUrl(dto.logoUrl()));

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

    private String getMimeType(byte[] data) {
        if (data == null || data.length < 4) {
            return "image/png"; // default fallback
        }
        // PNG magic bytes: 89 50 4E 47
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return "image/png";
        }
        // JPEG magic bytes: FF D8 FF
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        // GIF magic bytes: 47 49 46 38 ('GIF8')
        if (data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x38) {
            return "image/gif";
        }
        // SVG starts with '<' (0x3C)
        if (data[0] == (byte) 0x3C) {
            return "image/svg+xml";
        }
        return "image/png"; // fallback
    }

    private byte[] parseLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) {
            return null;
        }
        if (logoUrl.startsWith("data:image/")) {
            int commaIndex = logoUrl.indexOf(",");
            if (commaIndex != -1) {
                String base64Data = logoUrl.substring(commaIndex + 1);
                try {
                    return java.util.Base64.getDecoder().decode(base64Data);
                } catch (IllegalArgumentException e) {
                    return logoUrl.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        try {
            return java.util.Base64.getDecoder().decode(logoUrl);
        } catch (IllegalArgumentException e) {
            return logoUrl.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
