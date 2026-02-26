package com.restaurante.resturante.mapper.maestros;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SucursalDtoMapper {

    // Convierte de entidad a DTO (Para respuesta de API)
    public SucursalDto toDto(Sucursal sucursal) {
        if (sucursal == null)
            return null;

        return new SucursalDto(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getTelefono(),
                sucursal.getEmpresa() != null ? sucursal.getEmpresa().getRazonSocial() : null);
    }

    // Convierte de CreateSucursalDto a Entidad (Para guardar)
    public Sucursal toEntity(CreateSucursalDto dto) {
        if (dto == null)
            return null;

        // Validación para evitar Nulos
        String nombreSeguro = Objects.requireNonNull(dto.nombre(), "El nombre de la Sucursal es requerida.");

        return Sucursal.builder()
                .nombre(nombreSeguro.toUpperCase())
                .direccion(dto.direccion())
                .telefono(dto.telefono())
                .estado(true)
                .build();
    }

    // Actualizar entidad
    public void updateEntity(CreateSucursalDto dto, Sucursal entity) {
        if (dto == null || entity == null)
            return;

        if (dto.nombre() != null) {
            entity.setNombre(dto.nombre().toUpperCase());
        }
        if (dto.direccion() != null) {
            entity.setDireccion(dto.direccion());
        }
        if (dto.telefono() != null) {
            entity.setTelefono(dto.telefono());
        }
    }
}
