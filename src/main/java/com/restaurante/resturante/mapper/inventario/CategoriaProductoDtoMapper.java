package com.restaurante.resturante.mapper.inventario;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.inventario.CategoriaProducto;
import com.restaurante.resturante.domain.maestros.Empresa;
import com.restaurante.resturante.dto.inventario.CategoriaProductoDto;

@Component
public class CategoriaProductoDtoMapper {

    public CategoriaProductoDto toDto(CategoriaProducto entity) {
        return new CategoriaProductoDto(
                entity.getIdCategoria(),
                entity.getNombreCategoria(),
                entity.getEmpresa().getId());
    }

    public CategoriaProducto toEntity(CategoriaProductoDto dto, Empresa empresa) {
        return CategoriaProducto.builder()
                .idCategoria(dto.id())
                .nombreCategoria(dto.nombre())
                .empresa(empresa)
                .build();
    }
}
