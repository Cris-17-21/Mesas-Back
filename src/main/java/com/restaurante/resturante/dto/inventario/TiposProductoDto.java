package com.restaurante.resturante.dto.inventario;

public record TiposProductoDto(
        Integer idTipo,
        String nombreTipo,
        Integer idCategoria,
        String nombreCategoria) {
}
