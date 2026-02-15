package com.restaurante.resturante.mapper.maestros;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.maestro.CreateSucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SucursalDtoMapper {
    /**
     * Convierte una entidad Sucursal a su DTO de respuesta.
     * Se utiliza principalmente en el flujo de login multi-sede.
     */
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

    /**
     * Convierte una lista de entidades a una lista de DTOs.
     */
    public List<SucursalDto> toDtoList(List<Sucursal> sucursales) {
        if (sucursales == null)
            return null;
        return sucursales.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Sucursal toEntity(CreateSucursalDto dto) {
        if (dto == null)
            return null;

        // Reutilizamos el método de abajo pasándole los datos del record
        return toEntity(dto.nombre(), dto.direccion(), dto.telefono());
    }

    /**
     * Crea una entidad Sucursal a partir de datos básicos (si lo necesitas más
     * adelante)
     */
    public Sucursal toEntity(String nombre, String direccion, String telefono) {
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion);
        sucursal.setTelefono(telefono);
        // Usamos setEstado(true) como lo tenías definido
        sucursal.setEstado(true); 
        return sucursal;
    }
}
