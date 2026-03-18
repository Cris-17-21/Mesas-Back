package com.restaurante.resturante.dto.inventario;

import java.math.BigDecimal;

public record PlatoSalesHistoryDto(
        Integer idProducto,
        String nombrePlato,
        Integer cantidadVendidaManana,
        Integer cantidadVendidaTarde,
        Integer cantidadVendidaNoche,
        Integer totalVendido,
        BigDecimal precioVenta
) {
}
