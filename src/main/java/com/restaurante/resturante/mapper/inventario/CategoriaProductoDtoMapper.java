package com.restaurante.resturante.mapper.inventario;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;

@Component
public class CategoriaProductoDtoMapper {

    public CategoriaProductoDto toDto(CategoriaProducto entity) {
        return new CategoriaProductoDto(
                entity.getIdCategoria(),
                entity.getNombreCategoria(),
                entity.getSucursal() != null ? entity.getSucursal().getId() : null);
    }

    public CategoriaProducto toEntity(CategoriaProductoDto dto, Sucursal sucursal) {
        return CategoriaProducto.builder()
                .idCategoria(dto.idCategoria())
                .nombreCategoria(dto.nombreCategoria())
                .sucursal(sucursal)
                .build();
    }
}
