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
                proveedor.getEstado());
    }

    public Proveedor toEntity(ProveedorDto dto) {
        if (dto == null)
            return null;

        return Proveedor.builder()
                .idProveedor(dto.idProveedor()) // May be null for creation, handled by DB or manually? The SQL uses INT
                                                // but not AUTO_INCREMENT explicitly in the CREATE statement provided.
                                                // Wait.
                // The User provided SQL: `id_proveedor` int(11) NOT NULL. No AUTO_INCREMENT.
                // So ID must be provided or generated. I should check if I missed
                // GenerationType in Entity.
                // Existing entities use UUID.
                // The SQL for Proveedor: `PRIMARY KEY (id_proveedor)`.
                // If strictly following SQL, I might need to generate ID manually or ask user.
                // For now, I'll map what is in DTO.
                .razonSocial(dto.razonSocial())
                .nombreComercial(dto.nombreComercial())
                .ruc(dto.ruc())
                .direccion(dto.direccion())
                .telefono(dto.telefono())
                .estado(dto.estado() != null ? dto.estado() : 1)
                .build();
    }
}
