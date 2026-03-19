package com.restaurante.resturante.dto.inventario;

import java.math.BigDecimal;

public record PlatoSalesHistoryDto(
        Integer idProducto,
        String nombrePlato,
        String nombreCategoria,
        String horario,
        Integer cantidadVendida,
        BigDecimal precioVenta
) {
}
