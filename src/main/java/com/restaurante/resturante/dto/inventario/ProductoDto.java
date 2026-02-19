package com.restaurante.resturante.dto.inventario;

import java.math.BigDecimal;

public record ProductoDto(
                Integer idProducto,
                String nombreProducto,
                String descripcion,
                BigDecimal precioVenta,
                BigDecimal costoCompra,
                Integer idCategoria, // ID reference
                String nombreCategoria, // Read-only convenience
                Integer idProveedor, // ID reference
                String razonSocialProveedor, // Read-only convenience
                String tipo,
                java.util.List<Integer> idTipos,
                Integer pesoGramos,

                Boolean estado,
                String imagen) {
}
