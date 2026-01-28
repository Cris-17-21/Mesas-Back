package com.restaurante.resturante.mapper.maestros;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.domain.maestros.Sucursal;
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
                sucursal.getId(), // Asumiendo que ya cambiaste Long a String en la entidad
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

    /**
     * Crea una entidad Sucursal a partir de datos básicos (si lo necesitas más
     * adelante)
     */
    public Sucursal toEntity(String nombre, String direccion, String telefono, Empresa empresa) {
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(nombre);
        sucursal.setDireccion(direccion);
        sucursal.setTelefono(telefono);
        sucursal.setEmpresa(empresa);
        sucursal.setEstado(true);
        return sucursal;
    }
}
