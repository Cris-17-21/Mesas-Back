package com.restaurante.resturante.mapper.inventario;

import org.springframework.stereotype.Component;
import com.restaurante.resturante.domain.inventario.TiposProducto;
import com.restaurante.resturante.dto.inventario.TiposProductoDto;
import com.restaurante.resturante.domain.inventario.CategoriaProducto;

@Component
public class TiposProductoMapper {

    public TiposProductoDto toDto(TiposProducto entity) {
        if (entity == null)
            return null;
        return new TiposProductoDto(
                entity.getIdTipo(),
                entity.getNombreTipo(),
                entity.getCategoria() != null ? entity.getCategoria().getIdCategoria() : null,
                entity.getCategoria() != null ? entity.getCategoria().getNombreCategoria() : null);
    }

    public TiposProducto toEntity(TiposProductoDto dto, CategoriaProducto categoria) {
        if (dto == null)
            return null;
        return TiposProducto.builder()
                .idTipo(dto.idTipo())
                .nombreTipo(dto.nombreTipo())
                .categoria(categoria)
                .build();
    }
}
