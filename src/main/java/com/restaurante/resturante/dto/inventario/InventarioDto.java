package com.restaurante.resturante.dto.inventario;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDto {
    private Integer idProducto;
    private String nombreProducto;
    private BigDecimal costoCompra;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer idProveedor;
    private String nombreProveedor;
}
