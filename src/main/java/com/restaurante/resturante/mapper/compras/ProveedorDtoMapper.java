package com.restaurante.resturante.mapper.compras;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.compras.Proveedor;
import com.restaurante.resturante.dto.compras.ProveedorDto;

@Component
public class ProveedorDtoMapper {

    public ProveedorDto toDto(Proveedor proveedor) {
        if (proveedor == null)
            return null;
        return new ProveedorDto(
                proveedor.getIdProveedor(),
                proveedor.getRazonSocial(),
                proveedor.getNombreComercial(),
                proveedor.getRuc(),
                proveedor.getDireccion(),
                proveedor.getTelefono(),
                proveedor.getEstado(),
                proveedor.getMetodosPago() != null ? proveedor.getMetodosPago().stream()
                        .map(mp -> new com.restaurante.resturante.dto.compras.ProveedorMetodoPagoDto(
                                mp.getIdTipoPago(),
                                mp.getTiposPago() != null ? mp.getTiposPago().getTipoPago() : null,
                                mp.getDatosPago()))
                        .collect(java.util.stream.Collectors.toList())
                        : java.util.Collections.emptyList());
    }

    public Proveedor toEntity(ProveedorDto dto) {
        if (dto == null)
            return null;

        return Proveedor.builder()
                .idProveedor(dto.idProveedor())
                .razonSocial(dto.razonSocial())
                .nombreComercial(dto.nombreComercial())
                .ruc(dto.ruc())
                .direccion(dto.direccion())
                .telefono(dto.telefono())
                .estado(dto.estado() != null ? dto.estado() : 1)
                .build();
    }
}
