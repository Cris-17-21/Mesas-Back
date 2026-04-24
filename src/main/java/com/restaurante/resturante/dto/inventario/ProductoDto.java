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

                java.time.LocalDateTime fechaRegistro,
                Integer stock,
                String sucursalId,

                Boolean estado,
                String imagen,
                
                // Platos fields
                Boolean esPlato,
                String horarioDisponible,
                java.time.LocalDate fechaDisponible) {
}
